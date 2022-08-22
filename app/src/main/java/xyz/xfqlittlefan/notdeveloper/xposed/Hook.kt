package xyz.xfqlittlefan.notdeveloper.xposed

import android.content.ContentResolver
import android.provider.Settings
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import xyz.xfqlittlefan.notdeveloper.ADB_ENABLED
import xyz.xfqlittlefan.notdeveloper.ADB_WIFI_ENABLED
import xyz.xfqlittlefan.notdeveloper.DEVELOPMENT_SETTINGS_ENABLED

class Hook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam?.packageName?.startsWith("android") == true || lpparam?.packageName?.startsWith(
                "com.android"
            ) == true
        ) {
            return
        }

        val preferences = XSharedPreferences(this::class.java.`package`!!.name)

        XposedHelpers.findAndHookMethod(Settings.Global::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            Int::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (preferences.getBoolean(
                            DEVELOPMENT_SETTINGS_ENABLED,
                            false
                        ) && param.args[1] == DEVELOPMENT_SETTINGS_ENABLED
                    ) {
                        param.result = 0
                    }
                    if (preferences.getBoolean(ADB_ENABLED, true) && param.args[1] == ADB_ENABLED) {
                        param.result = 0
                    }
                    if (preferences.getBoolean(
                            ADB_WIFI_ENABLED,
                            true
                        ) && param.args[1] == ADB_WIFI_ENABLED
                    ) {
                        param.result = 0
                    }
                }
            })

        XposedHelpers.findAndHookMethod(Settings.Global::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (preferences.getBoolean(
                            DEVELOPMENT_SETTINGS_ENABLED,
                            false
                        ) && param.args[1] == DEVELOPMENT_SETTINGS_ENABLED
                    ) {
                        param.result = 0
                    }
                    if (preferences.getBoolean(ADB_ENABLED, true) && param.args[1] == ADB_ENABLED) {
                        param.result = 0
                    }
                    if (preferences.getBoolean(
                            ADB_WIFI_ENABLED,
                            true
                        ) && param.args[1] == ADB_WIFI_ENABLED
                    ) {
                        param.result = 0
                    }
                }
            })

        XposedHelpers.findAndHookMethod(Settings.Secure::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            Int::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (preferences.getBoolean(
                            DEVELOPMENT_SETTINGS_ENABLED,
                            false
                        ) && param.args[1] == DEVELOPMENT_SETTINGS_ENABLED
                    ) {
                        param.result = 0
                    }
                    if (preferences.getBoolean(ADB_ENABLED, true) && param.args[1] == ADB_ENABLED) {
                        param.result = 0
                    }
                }
            })

        XposedHelpers.findAndHookMethod(Settings.Global::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (preferences.getBoolean(
                            DEVELOPMENT_SETTINGS_ENABLED,
                            false
                        ) && param.args[1] == DEVELOPMENT_SETTINGS_ENABLED
                    ) {
                        param.result = 0
                    }
                    if (preferences.getBoolean(ADB_ENABLED, true) && param.args[1] == ADB_ENABLED) {
                        param.result = 0
                    }
                }
            })
    }
}