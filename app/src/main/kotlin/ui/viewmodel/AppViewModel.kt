package top.ltfan.notdeveloper.ui.viewmodel

import android.content.Context
import android.content.pm.PackageInfo
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.application.NotDevApplication
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.datastore.AppListSettings
import top.ltfan.notdeveloper.datastore.model.AppDataStore
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.service.SystemServiceClient
import top.ltfan.notdeveloper.service.systemService
import top.ltfan.notdeveloper.ui.page.Apps
import top.ltfan.notdeveloper.ui.page.Main
import top.ltfan.notdeveloper.ui.page.Overview
import top.ltfan.notdeveloper.ui.page.Page
import top.ltfan.notdeveloper.xposed.statusIsPreferencesReady

class AppViewModel(app: NotDevApplication) : AndroidViewModel<NotDevApplication>(app) {
    val settingsStore = AppListSettings.createDataStore()

    val hazeState = HazeState()

    val snackbarHostState = SnackbarHostState()

    val showNavBar: Boolean inline get() {
        return (currentPage != Apps || currentConfiguringPackageInfo == null)
    }
    val backStack = mutableStateListOf<Page>(Overview)
    val currentPage inline get() = backStack.last()
    val navBarEntry inline get() = backStack.last { it is Main }

    var isPreferencesReady by mutableStateOf(false)
    val testResults = mutableStateMapOf<DetectionMethod, Boolean>()
    var service: SystemServiceClient? by mutableStateOf(null)

    val myPackageInfo = application.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0)!!
    var currentConfiguringPackageInfo by mutableStateOf<PackageInfo?>(null)
    private var _users by mutableStateOf(queryUsers())
    val users get() = _users

    var selectedUser by settingsStore.propertyAsState(
        get = { it.selectedUser },
        set = { settings, user ->
            updateAppList(user)
            settings.copy(selectedUser = user)
        },
    )

    var appSortMethod by settingsStore.propertyAsState(
        get = { it.sort },
        set = { settings, sort -> settings.copy(sort = sort) },
    )

    var appFilteredMethods by settingsStore.propertyAsState(
        get = { it.filtered },
        set = { settings, filters -> settings.copy(filtered = filters) },
    )

    private var _appList by mutableStateOf(listOf<PackageInfo>())
    val appList get() = _appList
    private var _currentDatabaseListState by mutableStateOf(
        application.database.dao().getPackageInfoFlow().collectAsState(emptyList())
    )
    val databaseList by _currentDatabaseListState

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

    fun queryUsers() = service?.queryUsers() ?: listOf(UserInfo.current)

    fun updateUsers() {
        _users = queryUsers()
        if (selectedUser in users) return
        selectedUser = users.first()
    }

    fun queryAppList(userInfo: UserInfo = selectedUser): Pair<List<PackageInfo>, Boolean> {
        val list = service?.queryApps(userInfo.id)?.ifEmpty { null }
        return (list ?: listOf(myPackageInfo)) to (list == null)
    }

    fun updateAppList(list: List<PackageInfo>, userInfo: UserInfo = selectedUser) {
        _appList = list
        _currentDatabaseListState =
            application.database.dao().getPackageInfoFlow(userInfo.id).collectAsState(databaseList)
    }

    fun updateAppList(userInfo: UserInfo = selectedUser) {
        updateAppList(queryAppList(userInfo).first, userInfo)
    }

    private fun <T> Flow<T>.collectAsState(initial: T): MutableState<T> {
        val delegate = mutableStateOf(initial)
        viewModelScope.launch { collect { delegate.value = it } }
        return delegate
    }

    private fun <T, R> AppDataStore<T>.propertyAsState(
        defaultValue: T = this.defaultValue,
        get: (T) -> R,
        set: (T, R) -> T,
    ): MutableState<R> {
        val delegate = data.collectAsState(defaultValue)
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
