package top.ltfan.notdeveloper.ui.viewmodel

import android.content.Context
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.application.NotDevApplication
import top.ltfan.notdeveloper.data.PackageInfoWrapper
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.data.wrapped
import top.ltfan.notdeveloper.datastore.AppFilter
import top.ltfan.notdeveloper.datastore.AppListSettings
import top.ltfan.notdeveloper.datastore.GlobalPreferences
import top.ltfan.notdeveloper.datastore.model.AppDataStore
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.service.SystemServiceClient
import top.ltfan.notdeveloper.service.systemService
import top.ltfan.notdeveloper.ui.page.Apps
import top.ltfan.notdeveloper.ui.page.Apps.processed
import top.ltfan.notdeveloper.ui.page.Main
import top.ltfan.notdeveloper.ui.page.Overview
import top.ltfan.notdeveloper.ui.page.Page
import top.ltfan.notdeveloper.util.getUserId
import top.ltfan.notdeveloper.util.toAndroid
import top.ltfan.notdeveloper.xposed.statusIsPreferencesReady
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AppViewModel(app: NotDevApplication) : AndroidViewModel<NotDevApplication>(app) {
    val settingsStore = AppListSettings.createDataStore()
    val globalPreferencesStore = GlobalPreferences.createDataStore()

    val hazeState = HazeState()

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

    var useGlobalPreferences by globalPreferencesStore.propertyAsMutableState(
        get = { it.useGlobalPreferences },
        set = { settings, useGlobalPreferences ->
            settings.copy(useGlobalPreferences = useGlobalPreferences)
        },
    )

    var isPreferencesReady by mutableStateOf(false)
    var service: SystemServiceClient? by mutableStateOf(null)

    val myPackageInfo =
        application.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0).wrapped()

    val packageInfoConfiguringTransitionState = SeekableTransitionState<PackageInfoWrapper?>(null)
    val currentConfiguringPackageInfo inline get() = packageInfoConfiguringTransitionState.targetState

    private var _users by mutableStateOf(queryUsers())
    val users get() = _users

    val selectedUserFlow = settingsStore.propertyAsSharedFlow { it.selectedUser }
    var selectedUser by settingsStore.propertyAsMutableState(
        defaultValue = settingsStore.defaultValue,
        get = { it.selectedUser },
        set = { settings, user -> settings.copy(selectedUser = user) },
    )

    val appSortMethodFlow = settingsStore.propertyAsSharedFlow { it.sort }
    var appSortMethod by settingsStore.propertyAsMutableState(
        defaultValue = settingsStore.defaultValue,
        get = { it.sort },
        set = { settings, sort -> settings.copy(sort = sort) },
    )

    val appFilteredMethodsFlow = settingsStore.propertyAsSharedFlow { it.filtered }
    var appFilteredMethods by settingsStore.propertyAsMutableState(
        defaultValue = settingsStore.defaultValue,
        get = { it.filtered },
        set = { settings, filtered -> settings.copy(filtered = filtered) },
    )

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

    @OptIn(ExperimentalCoroutinesApi::class)
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

    private fun <T> Flow<T>.collectAsStateVM(initial: T): State<T> {
        val delegate = mutableStateOf(initial)
        viewModelScope.launch { collect { delegate.value = it } }
        return delegate
    }

    @OptIn(ExperimentalForInheritanceCoroutinesApi::class)
    private fun <T, R> AppDataStore<T>.propertyAsSharedFlow(
        transform: (T) -> R,
    ) = data.map { transform(it) }.shareIn(
        viewModelScope,
        started = SharingStarted.Eagerly,
        replay = 1,
    )

    private fun <T, R> AppDataStore<T>.propertyAsMutableState(
        defaultValue: T = this.defaultValue,
        get: (T) -> R,
        set: (T, R) -> T,
    ): ReadWriteProperty<Any?, R> {
        val delegate = data.collectAsStateVM(defaultValue)
        return object : ReadWriteProperty<Any?, R> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = get(delegate.value)
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: R) {
                viewModelScope.launch { updateData { set(it, value) } }
            }
        }
    }
}
