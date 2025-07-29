package top.ltfan.notdeveloper.xposed

import android.app.AndroidAppHelper
import android.os.Binder
import android.os.Bundle
import android.os.UserHandle
import androidx.annotation.Keep
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.broadcast.receiveChangeBroadcast
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod

@Keep
class Hook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
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

        notifyChange(lpparam)

        val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID)

        DetectionCategory.allMethods.forEach { method ->
            try {
                method.hook(prefs, lpparam)
            } catch (e: Throwable) {
                Log.e("Failed to apply hook for ${method::class.simpleName}: ${e.message}", e)
            }
        }
    }

    private fun DetectionMethod.SettingsMethod.doHook(
        prefs: XSharedPreferences,
        lpparam: LoadPackageParam
    ) {
        if (lpparam.packageName != "com.android.providers.settings") return

        Log.d("Processing SettingsMethod: $this")

        val settingsProviderClass = XposedHelpers.findClass(
            "com.android.providers.settings.SettingsProvider",
            lpparam.classLoader,
        )

        XposedBridge.hookAllMethods(
            settingsProviderClass,
            "packageValueForCallResult",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    prefs.reload()
                    val arg = param.args[1] as String

                    if (arg == settingKey) {
                        val uid = Binder.getCallingUid()
                        val packageName = AndroidAppHelper.currentApplication().packageManager
                            .getPackagesForUid(uid)?.firstOrNull() ?: return

//                        if (packageName != BuildConfig.APPLICATION_ID) {
//                            Log.d("Calling package is not the app itself, skipping hook $arg")
//                            return
//                        }

                        if (packageName.startsWith("android") || packageName.startsWith("com.android")) {
                            Log.d("Calling package is a system package, skipping hook $arg")
                            return
                        }

                        if (!prefs.getBoolean(preferenceKey, true)) {
                            Log.d("Skipping ${param.method.name}($arg) as preference is disabled")
                            return
                        }

                        Log.d("processing ${param.method.name} from ${lpparam.packageName} with arg $arg")

                        val result = param.result as Bundle
                        result.putString("value", "0")

                        Log.d("processed ${param.method.name}($arg): ${param.result}")
                    }
                }
            }
        )
    }

    private fun DetectionMethod.SystemPropertiesMethod.doHook(
        prefs: XSharedPreferences,
        lpparam: LoadPackageParam,
    ) {
        if (lpparam.packageName.startsWith("android") || lpparam.packageName.startsWith("com.android")) return

        Log.d("Processing SystemPropertiesMethod: $this for package ${lpparam.packageName}")

        val clazz = XposedHelpers.findClassIfExists(
            "android.os.SystemProperties", lpparam.classLoader
        )

        if (clazz == null) {
            Log.w("cannot find SystemProperties class")
            return
        }

        // TODO: Native hooks
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
                            param.result = getOverrideValue(methodName)
                            Log.d("processed ${param.method.name}($arg): ${param.result}")
                        }
                    }
                }
            )
        }
    }

    private fun notifyChange(lpparam: LoadPackageParam) {
        if (lpparam.packageName != "com.android.providers.settings") return

        val settingsProviderClass = XposedHelpers.findClass(
            "com.android.providers.settings.SettingsProvider",
            lpparam.classLoader,
        )

        XposedBridge.hookAllMethods(
            settingsProviderClass,
            "onCreate",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    Log.d("Got SettingsProvider")

                    val application = AndroidAppHelper.currentApplication()

                    application.receiveChangeBroadcast { method ->
                        Log.d("Received change broadcast for ${method.preferenceKey}")

                        when (method) {
                            is DetectionMethod.SettingsMethod -> {
                                val settingsProvider = param.thisObject

                                val settingsRegistryField = XposedHelpers.findField(
                                    settingsProvider::class.java,
                                    "mSettingsRegistry"
                                )
                                val settingsRegistry = settingsRegistryField.get(settingsProvider)

                                val notify = XposedHelpers.findMethodExact(
                                    settingsRegistry::class.java,
                                    "notifyForSettingsChange",
                                    Int::class.java,
                                    String::class.java,
                                )

                                val settingsStateClass = XposedHelpers.findClass(
                                    "com.android.providers.settings.SettingsState",
                                    lpparam.classLoader,
                                )

                                val userId = XposedHelpers.callStaticMethod(
                                    UserHandle::class.java,
                                    "myUserId",
                                ) as Int

                                val key = XposedHelpers.callStaticMethod(
                                    settingsStateClass,
                                    "makeKey",
                                    when (method.settingsClass) {
                                        android.provider.Settings.Global::class.java -> 0
                                        android.provider.Settings.System::class.java -> 1
                                        android.provider.Settings.Secure::class.java -> 2
                                        else -> return@receiveChangeBroadcast
                                    },
                                    userId,
                                )

                                notify.invoke(settingsRegistry, key, method.settingKey)

                                Log.d("Notified settings change for key: $key, setting: ${method.settingKey}")
                            }

                            else -> Unit
                        }
                    }

                    Log.d("Registered change broadcast receiver")
                }
            }
        )
    }

    private fun DetectionMethod.hook(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
        when (this) {
            is DetectionMethod.SettingsMethod -> doHook(prefs, lpparam)
            is DetectionMethod.SystemPropertiesMethod -> doHook(prefs, lpparam)
        }
    }
}
