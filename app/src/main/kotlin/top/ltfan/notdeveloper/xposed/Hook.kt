package top.ltfan.notdeveloper.xposed

import android.content.ContentResolver
import android.provider.Settings
import androidx.annotation.Keep
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import top.ltfan.notdeveloper.ADB_ENABLED
import top.ltfan.notdeveloper.ADB_WIFI_ENABLED
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.DEVELOPMENT_SETTINGS_ENABLED

@Keep
class Hook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName.startsWith("android") || lpparam.packageName.startsWith("com.android")) {
            return
        }

        Log.d("processing package ${lpparam.packageName}")

        if (lpparam.packageName == BuildConfig.APPLICATION_ID) {
            XposedHelpers.findAndHookMethod(
                "${BuildConfig.APPLICATION_ID}.xposed.ModuleStatusKt",
                lpparam.classLoader,
                "getStatusIsModuleActivated",
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
                changeResultToZero(
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
                changeResultToZero(lpparam, prefs, param, DEVELOPMENT_SETTINGS_ENABLED, ADB_ENABLED)
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

        processSystemProps(prefs, lpparam)
    }

    private fun processSystemProps(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
        val clazz = XposedHelpers.findClassIfExists(
            "android.os.SystemProperties", lpparam.classLoader
        )

        if (clazz == null) {
            Log.w("cannot find SystemProperties class")
            return
        }

        val ffsReady = "sys.usb.ffs.ready"
        val usbState = "sys.usb.state"
        val usbConfig = "sys.usb.config"
        val rebootFunc = "persist.sys.usb.reboot.func"
        val svcAdbd = "init.svc.adbd"

        val methodGet = "get"
        val methodGetprop = "getprop"
        val methodGetBoolean = "getBoolean"
        val methodGetInt = "getInt"
        val methodGetLong = "getLong"

        val overrideAdb = "mtp"
        val overrideSvcAdbd = "stopped"

        listOf(methodGet, methodGetprop, methodGetBoolean, methodGetInt, methodGetLong).forEach {
            XposedBridge.hookAllMethods(
                clazz, it,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        prefs.reload()
                        if (!prefs.getBoolean(ADB_ENABLED, true)) return

                        val arg = param.args[0] as String
                        Log.d("processing ${param.method.name} from ${lpparam.packageName} with arg $arg")

                        if (param.method.name != methodGet && arg != ffsReady) {
                            Log.i("props processed ${param.method.name} from ${lpparam.packageName} receiving invalid arg $arg")
                            return
                        }

                        when (arg) {
                            ffsReady -> {
                                when (param.method.name) {
                                    methodGet -> param.result = "0"
                                    methodGetprop -> param.result = "0"
                                    methodGetBoolean -> param.result = false
                                    methodGetInt -> param.result = 0
                                    methodGetLong -> param.result = 0L
                                }
                            }

                            usbState -> param.result = overrideAdb
                            usbConfig -> param.result = overrideAdb
                            rebootFunc -> param.result = overrideAdb
                            svcAdbd -> param.result = overrideSvcAdbd
                        }

                        Log.d("processed ${param.method.name}($arg): ${param.result}")
                    }
                }
            )
        }
    }

    private fun changeResultToZero(
        lpparam: LoadPackageParam,
        prefs: XSharedPreferences,
        param: MethodHookParam,
        vararg keys: String
    ) {
        val arg = param.args[1] as String
        Log.d("processing ${param.method.name} from ${lpparam.packageName} with arg $arg")

        keys.forEach { key ->
            if (prefs.getBoolean(key, true) && arg == key) {
                param.result = 0
                Log.d("processed ${param.method.name}($arg): ${param.result}")
                return
            }
        }

        Log.d("processed ${param.method.name}($arg) without changing result")
    }
}
