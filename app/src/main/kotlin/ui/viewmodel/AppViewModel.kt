package top.ltfan.notdeveloper.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.service.INotDevService
import top.ltfan.notdeveloper.ui.page.Main
import top.ltfan.notdeveloper.ui.page.Overview
import top.ltfan.notdeveloper.ui.page.Page
import top.ltfan.notdeveloper.xposed.Log
import top.ltfan.notdeveloper.xposed.notifySettingChange

class AppViewModel(app: Application) : AndroidViewModel(app) {
    var showNavBar by mutableStateOf(true)
    val backStack = mutableStateListOf<Page>(Overview)
    val currentPage inline get() = backStack.last()
    val navBarEntry inline get() = backStack.last { it is Main }

    var isPreferencesReady by mutableStateOf(false)
    val testResults = mutableStateMapOf<DetectionMethod, Boolean>()
    var service: INotDevService? by mutableStateOf(null)

    fun navigateMain(page: Main) {
        if (currentPage == page) return
        val existingIndex = backStack.indexOfFirst { it == page }

        if (existingIndex == -1) {
            backStack.add(page)
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
            Log.v("${method.preferenceKey} test result: $result")
        }
    }

    fun afterStatusChange(method: DetectionMethod) {
        when (method) {
            is DetectionMethod.SettingsMethod -> {
                val service = service
                if (service == null) {
                    Log.Android.w("Service not connected, cannot notify settings changes")
                    return
                }

                try {
                    service.notifySettingChange(method) {
                        test()
                    }
                } catch (e: Throwable) {
                    Log.Android.e("Failed to notify setting change for ${method.preferenceKey}", e)
                    test()
                }
            }

            is DetectionMethod.SystemPropertiesMethod -> test()
        }
    }
}
