package xyz.xfqlittlefan.notdeveloper.xposed

import android.content.ContentResolver
import android.provider.Settings
import androidx.annotation.Keep
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import xyz.xfqlittlefan.notdeveloper.ADB_ENABLED
import xyz.xfqlittlefan.notdeveloper.ADB_WIFI_ENABLED
import xyz.xfqlittlefan.notdeveloper.BuildConfig
import xyz.xfqlittlefan.notdeveloper.DEVELOPMENT_SETTINGS_ENABLED

@Keep
class Hook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName.startsWith("android") || lpparam.packageName.startsWith(
                "com.android"
            )
        ) {
            return
        }

        if (lpparam.packageName == BuildConfig.APPLICATION_ID) {
            XposedHelpers.findAndHookMethod(
                "xyz.xfqlittlefan.notdeveloper.xposed.ModuleStatusKt",
                lpparam.classLoader,
                "isModuleActive",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = true
                    }
                })
        }

        val preferences = XSharedPreferences(BuildConfig.APPLICATION_ID)

        XposedHelpers.findAndHookMethod(
            Settings.Global::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            Int::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    preferences.reload()
                    when {
                        preferences.getBoolean(
                            DEVELOPMENT_SETTINGS_ENABLED,
                            true
                        ) && param.args[1] == DEVELOPMENT_SETTINGS_ENABLED -> {
                            param.result = 0
                        }
                        preferences.getBoolean(
                            ADB_ENABLED,
                            true
                        ) && param.args[1] == ADB_ENABLED -> {
                            param.result = 0
                        }
                        preferences.getBoolean(
                            ADB_WIFI_ENABLED,
                            true
                        ) && param.args[1] == ADB_WIFI_ENABLED -> {
                            param.result = 0
                        }
                    }
                }
            })

        XposedHelpers.findAndHookMethod(
            Settings.Global::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    preferences.reload()
                    when {
                        preferences.getBoolean(
                            DEVELOPMENT_SETTINGS_ENABLED,
                            true
                        ) && param.args[1] == DEVELOPMENT_SETTINGS_ENABLED -> {
                            param.result = 0
                        }
                        preferences.getBoolean(
                            ADB_ENABLED,
                            true
                        ) && param.args[1] == ADB_ENABLED -> {
                            param.result = 0
                        }
                        preferences.getBoolean(
                            ADB_WIFI_ENABLED,
                            true
                        ) && param.args[1] == ADB_WIFI_ENABLED -> {
                            param.result = 0
                        }
                    }
                }
            })

        XposedHelpers.findAndHookMethod(
            Settings.Secure::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            Int::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    preferences.reload()
                    when {
                        preferences.getBoolean(
                            DEVELOPMENT_SETTINGS_ENABLED,
                            true
                        ) && param.args[1] == DEVELOPMENT_SETTINGS_ENABLED -> {
                            param.result = 0
                        }
                        preferences.getBoolean(
                            ADB_ENABLED,
                            true
                        ) && param.args[1] == ADB_ENABLED -> {
                            param.result = 0
                        }
                    }
                }
            })

        XposedHelpers.findAndHookMethod(
            Settings.Secure::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    preferences.reload()
                    when {
                        preferences.getBoolean(
                            DEVELOPMENT_SETTINGS_ENABLED,
                            true
                        ) && param.args[1] == DEVELOPMENT_SETTINGS_ENABLED -> {
                            param.result = 0
                        }
                        preferences.getBoolean(
                            ADB_ENABLED,
                            true
                        ) && param.args[1] == ADB_ENABLED -> {
                            param.result = 0
                        }
                    }
                }
            })

        // Call the hideSystemProps function
        hideSystemProps(lpparam)
    }

    // Define the hideSystemProps function
    fun hideSystemProps(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("LOAD " + lpparam.packageName)

        val clazz = XposedHelpers.findClassIfExists(
            "android.os.SystemProperties", lpparam.classLoader)

        XposedBridge.log("CLASS " + clazz?.name)

        if (clazz != null) {
            val ffsReady = "sys.usb.ffs.ready";
            val usbState = "sys.usb.state";
            val usbConfig = "sys.usb.config";
            val rebootFunc = "persist.sys.usb.reboot.func";
            val methodGet = "get"
            val methodGetBoolean = "getBoolean"
            val methodGetInt = "getInt"
            val methodGetLong = "getLong"
            val overrideAdb = "mtp"

            listOf(methodGet, methodGetBoolean, methodGetInt, methodGetLong).forEach {
                XposedBridge.hookAllMethods(clazz, it,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam?) {
                            XposedBridge.log(
                                "ACCESS " + param!!.method.name + " PARAM " + param.args[0])

                            if (param.args[0] != ffsReady && param.method.name != methodGet) {
                                return
                            }

                            when(param.args[0]) {
                                ffsReady -> {
                                    when(param.method.name) {
                                        methodGet -> param.result = "0"
                                        methodGetBoolean -> param.result = false
                                        methodGetInt -> param.result = 0
                                        methodGetLong -> param.result = 0L
                                    }
                                }
                                usbState -> param.result = overrideAdb
                                usbConfig -> param.result = overrideAdb
                                rebootFunc -> param.result = overrideAdb
                            }
                        }
                    })
            }
        }
    }
}
