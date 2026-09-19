package top.ltfan.notdeveloper.ui.viewmodel

import android.content.Context
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import top.ltfan.material.m3.settingspage.SettingsStoreDescriber
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.ModuleService
import top.ltfan.notdeveloper.application.NotDevApplication
import top.ltfan.notdeveloper.data.PackageInfoWrapper
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.data.wrapped
import top.ltfan.notdeveloper.datastore.AppFilter
import top.ltfan.notdeveloper.datastore.AppListSettingsStore
import top.ltfan.notdeveloper.datastore.GlobalPreferencesStore
import top.ltfan.notdeveloper.datastore.filtered
import top.ltfan.notdeveloper.datastore.selectedUser
import top.ltfan.notdeveloper.datastore.sort
import top.ltfan.notdeveloper.datastore.useGlobalPreferences
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.service.ScopeController
import top.ltfan.notdeveloper.service.SystemServiceClient
import top.ltfan.notdeveloper.service.systemService
import top.ltfan.notdeveloper.settings.UiSettingsModelStore
import top.ltfan.notdeveloper.settings.blur
import top.ltfan.notdeveloper.settings.settingsDescriber
import top.ltfan.notdeveloper.ui.page.Apps
import top.ltfan.notdeveloper.ui.page.Apps.processed
import top.ltfan.notdeveloper.ui.page.Main
import top.ltfan.notdeveloper.ui.page.Overview
import top.ltfan.notdeveloper.ui.page.Page
import top.ltfan.notdeveloper.util.getUserId
import top.ltfan.notdeveloper.util.toAndroid
import top.ltfan.notdeveloper.xposed.statusIsPreferencesReady
import kotlin.time.Duration.Companion.seconds

class AppViewModel(app: NotDevApplication) : AndroidViewModel<NotDevApplication>(app) {
    private val stores = app.storeHost
    private val appListSettingsStore = stores.mutable(AppListSettingsStore)
    private val uiSettingsStore = stores.mutable(UiSettingsModelStore)
    private val globalPreferencesStore = stores.mutable(GlobalPreferencesStore)

    /** `true` when every persisted store has read its first value. */
    var storesReady by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            withTimeoutOrNull(StoreReadyTimeout) {
                appListSettingsStore.awaitInitialized()
                uiSettingsStore.awaitInitialized()
                globalPreferencesStore.awaitInitialized()
            }
            storesReady = true
        }
    }

    /**
     * The settings page describer, backed by the persisted user interface
     * settings.
     */
    val settingsDescriber: SettingsStoreDescriber by lazy {
        uiSettingsStore.settingsDescriber(
            viewModelScope
        )
    }

    var blur by uiSettingsStore.blur

    /**
     * Latest global detection states, mirrored into the framework remote
     * preferences so the hooked packages read the same values.
     */
    private val globalDetectionStates = mutableMapOf<String, Boolean>()

    /**
     * Latest per-package detection states, keyed by `"package|method"`, also
     * mirrored into the remote preferences.
     */
    private val perAppDetectionStates = mutableMapOf<String, Boolean>()

    init {
        val dao = application.database.dao()
        DetectionCategory.allMethods.forEach { method ->
            viewModelScope.launch {
                dao.isGlobalDetectionEnabledFlow(method.name).collect { enabled ->
                    globalDetectionStates[method.name] = enabled
                    writeRemotePreferences()
                }
            }
        }
        viewModelScope.launch {
            dao.getAllDetectionsFlow().collect { detections ->
                perAppDetectionStates.clear()
                detections.forEach { detection ->
                    perAppDetectionStates["${detection.packageName}|${detection.methodName}"] =
                        detection.enabled
                }
                writeRemotePreferences()
            }
        }
        viewModelScope.launch {
            snapshotFlow { ModuleService.preferences }.collect { writeRemotePreferences() }
        }
    }

    private fun writeRemotePreferences() {
        val preferences = ModuleService.preferences ?: return
        preferences.edit {
            globalDetectionStates.forEach { (key, value) -> putBoolean(key, value) }
            perAppDetectionStates.forEach { (key, value) -> putBoolean(key, value) }
        }
    }

    val showNavBar: Boolean
        inline get() {
            return (currentPage != Apps || currentConfiguringPackageInfo == null)
        }
    val backStack = mutableStateListOf<Page>(Overview)
    val currentPage inline get() = backStack.last()
    val navBarEntry inline get() = backStack.last { it is Main }

    fun navigateMain(page: Main) {
        if (currentPage == page) {
            page.secondClick()
            return
        }
        val existingIndex = backStack.indexOfFirst { it == page }

        if (existingIndex == -1) {
            backStack.add(page)
            return
        }

        if (page is Overview) {
            backStack.removeRange(existingIndex + 1, backStack.size)
            return
        }

        val nextMainIndex = backStack.subList(existingIndex + 1, backStack.size)
            .indexOfFirst { it is Main }
            .let { if (it == -1) backStack.size else existingIndex + 1 + it }

        val pagesToMove = backStack.subList(existingIndex, nextMainIndex)
        backStack.addAll(pagesToMove)
        backStack.removeRange(existingIndex, nextMainIndex)
    }

    var useGlobalPreferences by globalPreferencesStore.useGlobalPreferences

    var isPreferencesReady by mutableStateOf(false)
    var service: SystemServiceClient? by mutableStateOf(null)

    val myPackageInfo =
        application.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0).wrapped()

    val packageInfoConfiguringTransitionState = SeekableTransitionState<PackageInfoWrapper?>(null)
    val currentConfiguringPackageInfo inline get() = packageInfoConfiguringTransitionState.targetState

    private var _users by mutableStateOf(queryUsers())
    val users get() = _users

    val selectedUserFlow = appListSettingsStore.data.map { it.selectedUser }.shareIn(
        viewModelScope,
        started = SharingStarted.Eagerly,
        replay = 1,
    )
    var selectedUser by appListSettingsStore.selectedUser

    val appSortMethodFlow = appListSettingsStore.data.map { it.sort }.shareIn(
        viewModelScope,
        started = SharingStarted.Eagerly,
        replay = 1,
    )
    var appSortMethod by appListSettingsStore.sort

    val appFilteredMethodsFlow = appListSettingsStore.data.map { it.filtered }.shareIn(
        viewModelScope,
        started = SharingStarted.Eagerly,
        replay = 1,
    )
    var appFilteredMethods by appListSettingsStore.filtered

    private var _isAppListError by mutableStateOf(false)
    val isAppListError get() = _isAppListError
    val appListErrorSnackbarTrigger = MutableSharedFlow<Unit?>()

    private var _isAppListUpdating by mutableStateOf(false)
    val isAppListUpdating get() = _isAppListUpdating

    private val appListUpdateTrigger = MutableSharedFlow<Unit>()
    val appListFlow = combine(selectedUserFlow, appListUpdateTrigger) { userInfo, _ ->
        queryAppList(userInfo)
    }.shareIn(viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    fun queryAppList(userInfo: UserInfo = selectedUser): Set<PackageInfoWrapper> {
        _isAppListUpdating = true
        val list = service?.queryApps(userInfo.id)?.ifEmpty { null }?.toSet().also {
            if (it == null) {
                _isAppListError = true
                viewModelScope.launch { appListErrorSnackbarTrigger.emit(Unit) }
                Log.Android.w("Failed to query apps for user ${userInfo.id}, service may not be connected")
            } else {
                _isAppListError = false
            }
        }
        return list ?: setOf(myPackageInfo)
    }

    fun updateAppList() {
        viewModelScope.launch {
            appListUpdateTrigger.emit(Unit)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val databaseListFlow = selectedUserFlow.flatMapLatest {
        queryDatabaseList(it)
    }.stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = emptySet())

    val appLists = combine(
        appListFlow,
        databaseListFlow,
        appSortMethodFlow,
        appFilteredMethodsFlow,
    ) { appList, databaseList, sortMethod, filteredMethods ->
        val filters = filteredMethods.subtract(AppFilter.groupingEntries)
        ((if (AppFilter.Configured !in filteredMethods) {
            databaseList.asSequence().processed(sortMethod, filters)
        } else emptyList()) to (if (AppFilter.Unconfigured !in filteredMethods) {
            appList.subtract(databaseList).asSequence().processed(sortMethod, filters)
        } else emptyList())).also {
            _isAppListUpdating = false
        }
    }.stateIn(
        viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList<PackageInfoWrapper>() to emptyList(),
    )

    @Composable
    fun collectAppLists(): Pair<List<PackageInfoWrapper>, List<PackageInfoWrapper>> {
        var configured by remember { mutableStateOf(appLists.value.first) }
        var unconfigured by remember { mutableStateOf(appLists.value.second) }

        LaunchedEffect(Unit) {
            appLists.collect { (c, u) ->
                configured = c
                unconfigured = u
            }
        }

        return configured to unconfigured
    }

    val globalDetectionTestTrigger =
        MutableSharedFlow<DetectionMethod>(replay = DetectionCategory.allMethods.size)

    fun test(method: DetectionMethod? = null) {
        if (method != null) {
            viewModelScope.launch { globalDetectionTestTrigger.emit(method) }
            return
        }

        DetectionCategory.allMethods.forEach { method ->
            viewModelScope.launch { globalDetectionTestTrigger.emit(method) }
        }
    }

    fun afterGlobalDetectionChange(method: DetectionMethod) {
        when (method) {
            is DetectionMethod.SettingsMethod -> {
                val service = service
                if (service == null) {
                    Log.Android.w("Service not connected, cannot notifySettingChange settings changes")
                    return
                }

                try {
                    service.notifySettingChange(method)
                } catch (e: Throwable) {
                    Log.Android.e("Failed to notifySettingChange setting change", e)
                } finally {
                    test(method)
                }
            }

            is DetectionMethod.SystemPropertiesMethod -> test(method)
        }
    }

    fun afterGlobalDetectionTest(method: DetectionMethod, result: Boolean) {
        Log.v("Global detection ${method.name} test result: $result")
    }

    context(context: Context)
    fun onResume() {
        isPreferencesReady = context.statusIsPreferencesReady
        connectService()
        test()
    }

    context(context: Context)
    fun connectService() {
        if (service == null) {
            service = context.systemService
        }
        ScopeController.sync(setOf("system"))
        updateUsers()
        updateAppList()
    }

    fun queryUsers() = service?.queryUsers() ?: listOf(
        UserInfo.current.copy(id = myPackageInfo.info.getUserId())
    )

    fun updateUsers() {
        _users = queryUsers()
        if (selectedUser in users) return
        selectedUser = users.first()
    }

    fun queryDatabaseList(userInfo: UserInfo? = null) =
        application.database.dao().let {
            if (userInfo != null) {
                it.getPackageInfoFlow(userInfo.id)
            } else {
                it.getPackageInfoFlow()
            }
        }.map { service?.queryApps(it)?.toSet() ?: it.toAndroid() }
}

private val StoreReadyTimeout = 2.seconds
