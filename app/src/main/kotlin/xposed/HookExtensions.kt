package top.ltfan.notdeveloper.xposed

import android.content.ContentResolver
import android.provider.Settings
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod

/**
 * 基类扩展方法，统一处理所有检测方法的hook
 */
fun DetectionMethod.hook(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
    when (this) {
        is DetectionCategory.DevelopmentMode.Development -> this.hookLogic(prefs, lpparam)
        is DetectionCategory.DevelopmentMode.DevelopmentLegacy -> this.hookLogic(prefs, lpparam)
        is DetectionCategory.UsbDebugging.Adb -> this.hookLogic(prefs, lpparam)
        is DetectionCategory.UsbDebugging.AdbLegacy -> this.hookLogic(prefs, lpparam)
        is DetectionCategory.UsbDebugging.AdbSystemPropsUsbState -> this.hookLogic(prefs, lpparam)
        is DetectionCategory.UsbDebugging.AdbSystemPropsUsbConfig -> this.hookLogic(prefs, lpparam)
        is DetectionCategory.UsbDebugging.AdbSystemPropsRebootFunc -> this.hookLogic(prefs, lpparam)
        is DetectionCategory.UsbDebugging.AdbSystemPropsSvcAdbd -> this.hookLogic(prefs, lpparam)
        is DetectionCategory.UsbDebugging.AdbSystemPropsFfsReady -> this.hookLogic(prefs, lpparam)
        is DetectionCategory.WirelessDebugging.AdbWifiEnabled -> this.hookLogic(prefs, lpparam)
        else -> error("This should not happen, unknown detection method: $this")
    }
}

/**
 * 公用的Settings Hook方法
 */
private fun hookSettings(
    settingsClass: Class<*>,
    settingKey: String,
    preferenceKey: String,
    prefs: XSharedPreferences,
    lpparam: LoadPackageParam
) {
    // Hook所有getInt方法重载
    XposedBridge.hookAllMethods(
        settingsClass,
        "getInt",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                prefs.reload()
                handleSettingsHook(lpparam, prefs, param, settingKey, preferenceKey)
            }
        }
    )
}

/**
 * 公用的SystemProperties Hook方法
 */
private fun hookSystemProperties(
    propertyKey: String,
    overrideValue: String,
    preferenceKey: String,
    prefs: XSharedPreferences,
    lpparam: LoadPackageParam,
    customOverride: ((String, String) -> Any?)? = null
) {
    val clazz = XposedHelpers.findClassIfExists(
        "android.os.SystemProperties", lpparam.classLoader
    )

    if (clazz == null) {
        Log.w("cannot find SystemProperties class")
        return
    }

    val methods = listOf("get", "getprop", "getBoolean", "getInt", "getLong")

    methods.forEach { methodName ->
        XposedBridge.hookAllMethods(
            clazz, methodName,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    prefs.reload()
                    if (!prefs.getBoolean(preferenceKey, true)) return

                    val arg = param.args[0] as String
                    Log.d("processing ${param.method.name} from ${lpparam.packageName} with arg $arg")

                    if (arg == propertyKey) {
                        param.result = if (customOverride != null) {
                            customOverride(methodName, arg)
                        } else {
                            when (methodName) {
                                "get", "getprop" -> overrideValue
                                "getBoolean" -> overrideValue.toBoolean()
                                "getInt" -> overrideValue.toIntOrNull() ?: 0
                                "getLong" -> overrideValue.toLongOrNull() ?: 0L
                                else -> overrideValue
                            }
                        }
                        Log.d("processed ${param.method.name}($arg): ${param.result}")
                    }
                }
            }
        )
    }
}

/**
 * Hook扩展方法，定义各个检测方法的hook逻辑
 */

fun DetectionCategory.DevelopmentMode.Development.hookLogic(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
    hookSettings(Settings.Global::class.java, "development_settings_enabled", preferenceKey, prefs, lpparam)
}

fun DetectionCategory.DevelopmentMode.DevelopmentLegacy.hookLogic(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
    hookSettings(Settings.Secure::class.java, "development_settings_enabled", preferenceKey, prefs, lpparam)
}

fun DetectionCategory.UsbDebugging.Adb.hookLogic(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
    hookSettings(Settings.Global::class.java, "adb_enabled", preferenceKey, prefs, lpparam)
}

fun DetectionCategory.UsbDebugging.AdbLegacy.hookLogic(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
    hookSettings(Settings.Secure::class.java, "adb_enabled", preferenceKey, prefs, lpparam)
}

fun DetectionCategory.WirelessDebugging.AdbWifiEnabled.hookLogic(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
    hookSettings(Settings.Global::class.java, "adb_wifi_enabled", preferenceKey, prefs, lpparam)
}

fun DetectionCategory.UsbDebugging.AdbSystemPropsUsbState.hookLogic(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
    hookSystemProperties("sys.usb.state", "mtp", preferenceKey, prefs, lpparam)
}

fun DetectionCategory.UsbDebugging.AdbSystemPropsUsbConfig.hookLogic(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
    hookSystemProperties("sys.usb.config", "mtp", preferenceKey, prefs, lpparam)
}

fun DetectionCategory.UsbDebugging.AdbSystemPropsRebootFunc.hookLogic(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
    hookSystemProperties("persist.sys.usb.reboot.func", "mtp", preferenceKey, prefs, lpparam)
}

fun DetectionCategory.UsbDebugging.AdbSystemPropsSvcAdbd.hookLogic(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
    hookSystemProperties("init.svc.adbd", "stopped", preferenceKey, prefs, lpparam)
}

fun DetectionCategory.UsbDebugging.AdbSystemPropsFfsReady.hookLogic(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
    hookSystemProperties("sys.usb.ffs.ready", "0", preferenceKey, prefs, lpparam) { methodName, _ ->
        when (methodName) {
            "get", "getprop" -> "0"
            "getBoolean" -> false
            "getInt" -> 0
            "getLong" -> 0L
            else -> "0"
        }
    }
}

/**
 * 处理Settings相关的hook
 */
private fun handleSettingsHook(
    lpparam: LoadPackageParam,
    prefs: XSharedPreferences,
    param: XC_MethodHook.MethodHookParam,
    settingKey: String,
    preferenceKey: String
) {
    val arg = param.args[1] as String
    Log.d("processing ${param.method.name} from ${lpparam.packageName} with arg $arg")

    if (arg == settingKey) {
        if (prefs.getBoolean(preferenceKey, true)) {
            param.result = 0
            Log.d("processed ${param.method.name}($arg): ${param.result}")
            return
        }
    }

    Log.d("processed ${param.method.name}($arg) without changing result")
}
