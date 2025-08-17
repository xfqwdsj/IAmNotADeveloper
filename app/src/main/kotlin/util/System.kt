package top.ltfan.notdeveloper.util

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.UserHandle
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.staticFunctions

val isMiui: Boolean
    @SuppressLint("PrivateApi") get() {
        val clazz = Class.forName("android.os.SystemProperties").kotlin
        val function =
            clazz.declaredFunctions.firstOrNull { it.name == "get" && it.parameters.size == 1 }
        return function?.call("ro.miui.ui.version.name") != ""
    }

context(viewModel: AppViewModel)
fun getPackageInfo(databaseInfo: top.ltfan.notdeveloper.database.PackageInfo): PackageInfo? {
    val packageManager = viewModel.application.packageManager
    return try {
        packageManager.getPackageInfo(databaseInfo.packageName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        Log.Android.w("Package not found: ${databaseInfo.packageName}", e)
        null
    }
}

context(viewModel: AppViewModel)
fun List<top.ltfan.notdeveloper.database.PackageInfo>.toAndroid() =
    mapNotNull { getPackageInfo(it) }

fun getUserId(uid: Int): Int {
    val function = UserHandle::class.staticFunctions.first { it.name == "getUserId" }
    return function.call(uid) as Int
}

fun getAppId(uid: Int): Int {
    val function = UserHandle::class.staticFunctions.first { it.name == "getAppId" }
    return function.call(uid) as Int
}
