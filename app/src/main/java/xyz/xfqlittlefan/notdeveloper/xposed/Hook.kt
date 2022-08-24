package xyz.xfqlittlefan.notdeveloper.xposed

import android.content.ContentResolver
import android.net.Uri
import android.provider.Settings
import androidx.annotation.Keep
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import xyz.xfqlittlefan.notdeveloper.BuildConfig
import xyz.xfqlittlefan.notdeveloper.preferences.ADB_ENABLED
import xyz.xfqlittlefan.notdeveloper.preferences.ADB_WIFI_ENABLED
import xyz.xfqlittlefan.notdeveloper.preferences.DEVELOPMENT_SETTINGS_ENABLED

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

        XposedHelpers.findAndHookMethod(
            Settings.Global::class.java,
            "getInt",
            ContentResolver::class.java,
            String::class.java,
            Int::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    when {
                        param.args[0].getPreference(DEVELOPMENT_SETTINGS_ENABLED) && param.args[1] == DEVELOPMENT_SETTINGS_ENABLED -> {
                            param.result = 0
                        }
                        param.args[0].getPreference(ADB_ENABLED) && param.args[1] == ADB_ENABLED -> {
                            param.result = 0
                        }
                        param.args[0].getPreference(ADB_WIFI_ENABLED) && param.args[1] == ADB_WIFI_ENABLED -> {
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
                    when {
                        param.args[0].getPreference(DEVELOPMENT_SETTINGS_ENABLED) && param.args[1] == DEVELOPMENT_SETTINGS_ENABLED -> {
                            param.result = 0
                        }
                        param.args[0].getPreference(ADB_ENABLED) && param.args[1] == ADB_ENABLED -> {
                            param.result = 0
                        }
                        param.args[0].getPreference(ADB_WIFI_ENABLED) && param.args[1] == ADB_WIFI_ENABLED -> {
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
                    when {
                        param.args[0].getPreference(DEVELOPMENT_SETTINGS_ENABLED) && param.args[1] == DEVELOPMENT_SETTINGS_ENABLED -> {
                            param.result = 0
                        }
                        param.args[0].getPreference(ADB_ENABLED) && param.args[1] == ADB_ENABLED -> {
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
                    when {
                        param.args[0].getPreference(DEVELOPMENT_SETTINGS_ENABLED) && param.args[1] == DEVELOPMENT_SETTINGS_ENABLED -> {
                            param.result = 0
                        }
                        param.args[0].getPreference(ADB_ENABLED) && param.args[1] == ADB_ENABLED -> {
                            param.result = 0
                        }
                    }
                }
            })
    }

    fun Any.getPreference(key: String): Boolean {
        val cursor = (this as? ContentResolver)?.query(Uri.Builder().apply {
            scheme("content")
            authority("xyz.xfqlittlefan.notdeveloper")
            appendPath(key)
        }.build(), null, null, null, null)
        if (cursor?.moveToFirst() == false) return true
        val result = cursor?.getString(0)?.toBooleanStrictOrNull() ?: true
        cursor?.close()
        return result
    }
}