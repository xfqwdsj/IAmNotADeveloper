package top.ltfan.notdeveloper.ui.viewmodel

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chrisbanes.haze.HazeState
import top.ltfan.notdeveloper.application.NotDevApplication
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.service.SystemServiceClient
import top.ltfan.notdeveloper.service.systemService
import top.ltfan.notdeveloper.ui.page.Main
import top.ltfan.notdeveloper.ui.page.Overview
import top.ltfan.notdeveloper.ui.page.Page
import top.ltfan.notdeveloper.xposed.statusIsPreferencesReady

class AppViewModel(app: NotDevApplication) : AndroidViewModel<NotDevApplication>(app) {
    val hazeState = HazeState()

    val snackbarHostState = SnackbarHostState()

    var showNavBar by mutableStateOf(true)
    val backStack = mutableStateListOf<Page>(Overview)
    val currentPage inline get() = backStack.last()
    val navBarEntry inline get() = backStack.last { it is Main }

    var isPreferencesReady by mutableStateOf(false)
    val testResults = mutableStateMapOf<DetectionMethod, Boolean>()
    var service: SystemServiceClient? by mutableStateOf(null)

    var users by mutableStateOf(queryUsers())

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
        if (service != null) return
        service = context.systemService
        updateUsers()
    }

    fun queryUsers() = service?.queryUsers() ?: emptyList()

    fun updateUsers(): List<UserInfo> {
        val list = queryUsers()
        users = list
        return list
    }
}
