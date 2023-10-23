package xyz.xfqlittlefan.notdeveloper.xposed

import android.content.ContentResolver
import android.provider.Settings
import android.util.Log
import androidx.annotation.Keep
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import xyz.xfqlittlefan.notdeveloper.ADB_ENABLED
import xyz.xfqlittlefan.notdeveloper.ADB_WIFI_ENABLED
import xyz.xfqlittlefan.notdeveloper.BuildConfig
import xyz.xfqlittlefan.notdeveloper.DEVELOPMENT_SETTINGS_ENABLED

@Keep
class Hook : IXposedHookLoadPackage {
    private val tag = "NotDeveloper"

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName.startsWith("android") || lpparam.packageName.startsWith("com.android")) {
            return
        }

        Log.i(tag, "processing " + lpparam.packageName)

        if (lpparam.packageName == BuildConfig.APPLICATION_ID) {
            XposedHelpers.findAndHookMethod(
                "xyz.xfqlittlefan.notdeveloper.xposed.ModuleStatusKt",
                lpparam.classLoader,
                "isModuleActive",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = true
                    }
                },
            )
        }

        val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID)

        val newApiCallback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                prefs.reload()
                hookResultToZero(
                    lpparam,
                    prefs,
                    param,
                    DEVELOPMENT_SETTINGS_ENABLED,
                    ADB_ENABLED,
                    ADB_WIFI_ENABLED
                )
            }
        }

        val oldApiCallback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                prefs.reload()
                hookResultToZero(lpparam, prefs, param, DEVELOPMENT_SETTINGS_ENABLED, ADB_ENABLED)
            }
        }

        XposedHelpers.findAndHookMethod(
            Settings.Global::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            Int::class.java,
            newApiCallback,
        )

        XposedHelpers.findAndHookMethod(
            Settings.Global::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            newApiCallback,
        )

        XposedHelpers.findAndHookMethod(
            Settings.Secure::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            Int::class.java,
            oldApiCallback,
        )

        XposedHelpers.findAndHookMethod(
            Settings.Secure::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            oldApiCallback,
        )

        if (prefs.getBoolean(ADB_ENABLED, true)) {
            hideSystemProps(lpparam)
        }
    }

    private fun hideSystemProps(lpparam: LoadPackageParam) {
        val clazz = XposedHelpers.findClassIfExists(
            "android.os.SystemProperties", lpparam.classLoader
        )

        if (clazz == null) {
            XposedBridge.log("$tag: cannot find SystemProperties class")
            return
        }

        val ffsReady = "sys.usb.ffs.ready"
        val usbState = "sys.usb.state"
        val usbConfig = "sys.usb.config"
        val rebootFunc = "persist.sys.usb.reboot.func"
        val svcadbd= "init.svc.adbd"
        val methodGet = "get"
        val methodGetProp = "getprop"
        val methodGetBoolean = "getBoolean"
        val methodGetInt = "getInt"
        val methodGetLong = "getLong"
        val overrideAdb = "mtp"
        val overridesvcadbd = "stopped"

        listOf(methodGet, methodGetBoolean, methodGetInt, methodGetLong).forEach {
            XposedBridge.hookAllMethods(
                clazz, it,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val arg = param.args[0] as String
                        XposedBridge.log("$tag: processing ${param.method.name} from ${lpparam.packageName} with arg $arg")

                        if (arg != ffsReady && param.method.name != methodGet) {
                            XposedBridge.log("$tag: processed ${param.method.name} from ${lpparam.packageName} receiving invalid arg $arg")
                            return
                        }

                        when (arg) {
                            ffsReady -> {
                                when (param.method.name) {
                                    methodGet -> param.result = "0"
                                    methodGetProp -> param.result = "0"
                                    methodGetBoolean -> param.result = false
                                    methodGetInt -> param.result = 0
                                    methodGetLong -> param.result = 0L
                                }
                            }

                            usbState -> param.result = overrideAdb
                            usbConfig -> param.result = overrideAdb
                            rebootFunc -> param.result = overrideAdb
                            svcadbd -> param.result = overridesvcadbd
                            
                        }

                        XposedBridge.log("$tag: hooked ${param.method.name}($arg): ${param.result} for ${lpparam.packageName}")
                    }
                },
            )
        }
    }

    private fun hookResultToZero(
        lpparam: LoadPackageParam,
        preferences: XSharedPreferences,
        param: MethodHookParam,
        vararg keys: String
    ) {
        val arg = param.args[1] as String
        XposedBridge.log("$tag: processing ${param.method.name} from ${lpparam.packageName} with arg $arg")

        keys.forEach { key ->
            if (preferences.getBoolean(key, true) && arg == key) {
                param.result = 0
                XposedBridge.log("$tag: hooked ${param.method.name}($arg): ${param.result}")
                return
            }
        }

        XposedBridge.log("$tag: processed ${param.method.name} without changing result")
    }
}
