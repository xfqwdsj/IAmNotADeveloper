package top.ltfan.notdeveloper.util

import android.annotation.SuppressLint
import android.os.UserHandle
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.staticFunctions

val isMiui: Boolean
    @SuppressLint("PrivateApi") get() {
        val clazz = Class.forName("android.os.SystemProperties").kotlin
        val method =
            clazz.declaredFunctions.firstOrNull { it.name == "get" && it.parameters.size == 1 }
        return method?.call("ro.miui.ui.version.name") != ""
    }

fun getUserId(uid: Int): Int {
    val function = UserHandle::class.staticFunctions.first { it.name == "getUserId" }
    return function.call(uid) as Int
}

fun getAppId(uid: Int): Int {
    val function = UserHandle::class.staticFunctions.first { it.name == "getAppId" }
    return function.call(uid) as Int
}
