package top.ltfan.notdeveloper.xposed

import android.provider.Settings
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod

fun DetectionMethod.hook(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
    when (this) {
        is DetectionCategory.DevelopmentMode.Development -> {
            hookSettings(Settings.Global::class.java, "development_settings_enabled", preferenceKey, prefs, lpparam)
        }
        is DetectionCategory.DevelopmentMode.DevelopmentLegacy -> {
            hookSettings(Settings.Secure::class.java, "development_settings_enabled", preferenceKey, prefs, lpparam)
        }
        is DetectionCategory.UsbDebugging.Adb -> {
            hookSettings(Settings.Global::class.java, "adb_enabled", preferenceKey, prefs, lpparam)
        }
        is DetectionCategory.UsbDebugging.AdbLegacy -> {
            hookSettings(Settings.Secure::class.java, "adb_enabled", preferenceKey, prefs, lpparam)
        }
        is DetectionCategory.UsbDebugging.AdbSystemPropsUsbState -> {
            hookSystemProperties("sys.usb.state", "mtp", preferenceKey, prefs, lpparam)
        }
        is DetectionCategory.UsbDebugging.AdbSystemPropsUsbConfig -> {
            hookSystemProperties("sys.usb.config", "mtp", preferenceKey, prefs, lpparam)
        }
        is DetectionCategory.UsbDebugging.AdbSystemPropsRebootFunc -> {
            hookSystemProperties("persist.sys.usb.reboot.func", "mtp", preferenceKey, prefs, lpparam)
        }
        is DetectionCategory.UsbDebugging.AdbSystemPropsSvcAdbd -> {
            hookSystemProperties("init.svc.adbd", "stopped", preferenceKey, prefs, lpparam)
        }
        is DetectionCategory.UsbDebugging.AdbSystemPropsFfsReady -> {
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
        is DetectionCategory.WirelessDebugging.AdbWifiEnabled -> {
            hookSettings(Settings.Global::class.java, "adb_wifi_enabled", preferenceKey, prefs, lpparam)
        }
        else -> error("This should not happen, unknown detection method: $this")
    }
}

private fun hookSettings(
    settingsClass: Class<*>,
    settingKey: String,
    preferenceKey: String,
    prefs: XSharedPreferences,
    lpparam: LoadPackageParam
) {
    XposedBridge.hookAllMethods(
        settingsClass,
        "getStringForUser",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                prefs.reload()
                handleSettingsHook(lpparam, prefs, param, settingKey, preferenceKey)
            }
        }
    )
}

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
            param.result = "0"
            Log.d("processed ${param.method.name}($arg): ${param.result}")
            return
        }
    }

    Log.d("processed ${param.method.name}($arg) without changing result")
}
