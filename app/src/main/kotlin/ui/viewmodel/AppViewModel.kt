package top.ltfan.notdeveloper.ui.viewmodel

import android.content.Context
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
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

    var useGlobalPreferences by globalPreferencesStore.propertyAsState(
        get = { it.useGlobalPreferences },
        set = { settings, useGlobalPreferences ->
            settings.copy(useGlobalPreferences = useGlobalPreferences)
        },
    )

    var isPreferencesReady by mutableStateOf(false)
    val testResults = mutableStateMapOf<DetectionMethod, Boolean>()
    var service: SystemServiceClient? by mutableStateOf(null)

    val myPackageInfo =
        application.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0).wrapped()

    val packageInfoConfiguringTransitionState = SeekableTransitionState<PackageInfoWrapper?>(null)
    val currentConfiguringPackageInfo inline get() = packageInfoConfiguringTransitionState.targetState

    private var _users by mutableStateOf(queryUsers())
    val users get() = _users

    val selectedUserFlow = settingsStore.propertyAsFlow(
        get = { it.selectedUser },
        set = { settings, user ->
            settings.copy(selectedUser = user)
        },
    )

    var selectedUser by selectedUserFlow.collectAsStateVM()

    val appSortMethodFlow = settingsStore.propertyAsFlow(
        get = { it.sort },
        set = { settings, sort -> settings.copy(sort = sort) },
    )

    var appSortMethod by appSortMethodFlow.collectAsStateVM()

    val appFilteredMethodsFlow = settingsStore.propertyAsFlow(
        get = { it.filtered },
        set = { settings, filters -> settings.copy(filtered = filters) },
    )

    var appFilteredMethods by appFilteredMethodsFlow.collectAsStateVM()

    private var _isAppListError by mutableStateOf(false)
    val isAppListError get() = _isAppListError
    val appListErrorSnackbarTrigger = MutableSharedFlow<Unit?>()

    private var _isAppListUpdating by mutableStateOf(false)
    val isAppListUpdating get() = _isAppListUpdating

    private val appListUpdateTrigger = MutableSharedFlow<Unit>()
    val appList = combine(selectedUserFlow, appListUpdateTrigger) { userInfo, _ ->
        queryAppList(userInfo)
    }.stateIn(
        viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = queryAppList(selectedUserFlow.value),
    )

    fun queryAppList(userInfo: UserInfo = selectedUserFlow.value): Set<PackageInfoWrapper> {
        _isAppListUpdating = true
        val list = service?.queryApps(userInfo.id)?.ifEmpty { null }?.toSet().also {
            if (it == null) {
                _isAppListError = true
                viewModelScope.launch {
                    appListErrorSnackbarTrigger.emit(Unit)
                }
                Log.Android.w("Failed to query apps for user ${userInfo.id}, service may not be connected")
            } else {
                _isAppListError = false
            }
        }
        return (list ?: setOf(myPackageInfo)).also {
            _isAppListUpdating = false
        }
    }

    fun updateAppList() {
        viewModelScope.launch {
            appListUpdateTrigger.emit(Unit)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val databaseList = selectedUserFlow.flatMapLatest {
        queryDatabaseList(it)
    }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = emptySet())

    @OptIn(ExperimentalCoroutinesApi::class)
    val appLists = combine(
        appList,
        databaseList,
        appSortMethodFlow,
        appFilteredMethodsFlow,
    ) { appList, databaseList, sortMethod, filteredMethods ->
        val filters = filteredMethods.subtract(AppFilter.groupingEntries)
        (if (AppFilter.Configured !in filteredMethods) {
            databaseList.asSequence().processed(sortMethod, filters)
        } else emptyList()) to (if (AppFilter.Unconfigured !in filteredMethods) {
            appList.subtract(databaseList).asSequence().processed(sortMethod, filters)
        } else emptyList())
    }.let { flow ->
        val configured = flow.mapLatest { it.first }
        val unconfigured = flow.mapLatest { it.second }
        configured to unconfigured
    }

    val configuredAppList = appLists.first.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList(),
    )

    val unconfiguredAppList = appLists.second.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList(),
    )

    @Composable
    fun collectAppLists(): Pair<List<PackageInfoWrapper>, List<PackageInfoWrapper>> {
        val configuredInitial = remember { configuredAppList.value }
        val unconfiguredInitial = remember { unconfiguredAppList.value }

        val configured by configuredAppList.collectAsState(configuredInitial)
        val unconfigured by unconfiguredAppList.collectAsState(unconfiguredInitial)
        return configured to unconfigured
    }

    fun navigateMain(page: Main) {
        if (currentPage == page) return
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

    fun test() {
        DetectionCategory.allMethods.forEach { method ->
            val result = method.test(application)
            testResults[method] = result
            Log.v("${method.name} test result: $result")
        }
    }

    fun afterStatusChange(method: DetectionMethod) {
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
                    test()
                }
            }

            is DetectionMethod.SystemPropertiesMethod -> test()
        }
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
    }

    fun queryUsers() = service?.queryUsers() ?: listOf(
        UserInfo.current.copy(id = myPackageInfo.info.getUserId())
    )

    fun updateUsers() {
        _users = queryUsers()
        if (selectedUserFlow.value in users) return
        selectedUserFlow.value = users.first()
    }

    fun queryDatabaseList(userInfo: UserInfo? = null) =
        application.database.dao().let {
            if (userInfo != null) {
                it.getPackageInfoFlow(userInfo.id)
            } else {
                it.getPackageInfoFlow()
            }
        }.map { service?.queryApps(it)?.toSet() ?: it.toAndroid() }

    private fun <T> MutableStateFlow<T>.collectAsStateVM(): MutableState<T> {
        val delegate = mutableStateOf(value)
        viewModelScope.launch { collect { delegate.value = it } }
        return object : MutableState<T> by delegate {
            override var value: T
                get() = delegate.value
                set(newValue) {
                    this@collectAsStateVM.value = newValue
                }
        }
    }

    private fun <T> Flow<T>.collectAsStateVM(initial: T): State<T> {
        val delegate = mutableStateOf(initial)
        viewModelScope.launch { collect { delegate.value = it } }
        return delegate
    }

    @OptIn(ExperimentalForInheritanceCoroutinesApi::class)
    private fun <T, R> AppDataStore<T>.propertyAsFlow(
        defaultValue: T = this.defaultValue,
        get: (T) -> R,
        set: (T, R) -> T,
    ): MutableStateFlow<R> {
        val dataFlow = data.map { get(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, get(defaultValue))

        val flow = MutableStateFlow(dataFlow.value)

        viewModelScope.launch {
            dataFlow.collect { value ->
                flow.value = value
            }
        }

        viewModelScope.launch {
            flow.collect { value ->
                updateData { set(it, value) }
            }
        }

        return flow
    }

    private fun <T, R> AppDataStore<T>.propertyAsState(
        defaultValue: T = this.defaultValue,
        get: (T) -> R,
        set: (T, R) -> T,
    ): MutableState<R> {
        val delegate = data.collectAsStateVM(defaultValue)
        return object : MutableState<R> by mutableStateOf(get(delegate.value)) {
            override var value: R
                get() = get(delegate.value)
                set(newValue) {
                    viewModelScope.launch {
                        updateData { set(delegate.value, newValue) }
                    }
                }
        }
    }
}
