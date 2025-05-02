package top.ltfan.notdeveloper.util

import android.annotation.SuppressLint
import kotlin.reflect.full.declaredFunctions

val isMiui: Boolean
    @SuppressLint("PrivateApi") get() {
        val clazz = Class.forName("android.os.SystemProperties").kotlin
        val method =
            clazz.declaredFunctions.firstOrNull { it.name == "get" && it.parameters.size == 1 }
        return method?.call("ro.miui.ui.version.name") != ""
    }
