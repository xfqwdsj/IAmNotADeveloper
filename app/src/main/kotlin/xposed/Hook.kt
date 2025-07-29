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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.broadcast.receiveChangeBroadcast
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod

@Keep
class Hook : IXposedHookLoadPackage {
    private val notification = MutableSharedFlow<Pair<SettingsChange, () -> Unit>>()

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
//        if (lpparam.packageName.startsWith("android") || lpparam.packageName.startsWith("com.android")) {
//            return
//        }

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

//            val notificationClass = XposedHelpers.findClass(
//                "${BuildConfig.APPLICATION_ID}.xposed.Notification",
//                lpparam.classLoader,
//            )
//
//            XposedBridge.hookAllMethods(
//                notificationClass,
//                Notification::notifySettingsChange.name,
//                object : XC_MethodHook() {
//                    override fun beforeHookedMethod(param: MethodHookParam) {
//                        Log.d("MainActivity notifyChange called")
//                        val type = param.args[0] as Int
//                        val name = param.args[1] as String
//                        val change = SettingsChange(type, name)
//                        val callback: () -> Unit = {
//                            XposedHelpers.callMethod(param.args[2], "invoke")
//                        }
//                        CoroutineScope(Dispatchers.Main).launch {
//                            Log.d("Emitting notification for change: type=$type, name=$name")
//                            notification.emit(change to callback)
//                            Log.d("Notification emitted for change: type=$type, name=$name")
//                        }
//                    }
//                }
//            )
        }

//        if (lpparam.packageName != "android") return
        if (lpparam.packageName != "com.android.providers.settings") return

        Log.d("processing package ${lpparam.packageName}")
        Log.d("processing process ${lpparam.processName}")

//        val f = File("/data/system/notdev.log")
//        if (!f.exists()) {
//            f.createNewFile()
//        }
//        @OptIn(ExperimentalTime::class)
//        f.appendText("===${Clock.System.now()} Started logging===\n")
//
//        @OptIn(ExperimentalTime::class)
//        fun testLog(msg: String) {
//            f.appendText("${Clock.System.now()} $msg\n")
//        }
//
//        val hook = object : XC_MethodHook() {
//            override fun afterHookedMethod(param: MethodHookParam) {
//                testLog("Hooked method: ${param.method.name} in ${lpparam.packageName}")
//                param.args.forEach {
//                    testLog("Argument: $it")
//                    if (it is Uri) {
//                        testLog("Found URI")
//                        if (it.authority != "settings") {
//                            testLog("URI authority is not 'settings', skipping")
//                            return
//                        }
//                    }
//                }
//                val uid = Binder.getCallingUid()
//                val packages = AndroidAppHelper.currentApplication().packageManager
//                    .getPackagesForUid(uid) ?: emptyArray()
//                packages.forEach {
//                    if (it != BuildConfig.APPLICATION_ID) return@forEach
//                    testLog("Calling package: $it")
//                }
//                val packageName = AndroidAppHelper.currentPackageName()
//                testLog("Calling package name: $packageName")
////                Thread.currentThread().getStackTrace().forEach {
////                    testLog("Stack trace: ${it.className}.${it.methodName} at line ${it.lineNumber}")
////                }
//                testLog("Result: ${param.result}")
//            }
//        }
//
//        Log.d("Try settings provider")
//
//        val settingsProvider = XposedHelpers.findClass(
//            "com.android.providers.settings.SettingsProvider",
//            lpparam.classLoader,
//        )
//
//        XposedBridge.hookAllMethods(
//            settingsProvider,
//            "getSettingLocked",
//            hook,
//        )
//
//        XposedBridge.hookAllMethods(
//            settingsProvider,
//            "packageValueForCallResult",
//            hook,
//        )
//
//        val settingsState = XposedHelpers.findClass(
//            "com.android.providers.settings.SettingsState",
//            lpparam.classLoader,
//        )
//
//        XposedBridge.hookAllMethods(
//            settingsState,
//            "getSettingLocked",
//            hook,
//        )
        notifyChange(lpparam)

        val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID)

        DetectionCategory.allMethods.forEach { method ->
            try {
                method.hook(prefs, lpparam)
                Log.d("Applied hook for ${method::class.simpleName}")
            } catch (e: Throwable) {
                Log.e("Failed to apply hook for ${method::class.simpleName}: ${e.message}", e)
            }
        }
    }

    private fun DetectionMethod.SettingsMethod.doHook(
        prefs: XSharedPreferences,
        lpparam: LoadPackageParam
    ) {
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
                        val packages = AndroidAppHelper.currentApplication().packageManager
                            .getPackagesForUid(uid) ?: return
                        if (packages.none { it == BuildConfig.APPLICATION_ID }) {
                            Log.d("Calling package is not the app itself, skipping hook $arg")
                            return
                        }

                        Log.d("processing ${param.method.name} from ${lpparam.packageName} with arg $arg")

                        if (prefs.getBoolean(preferenceKey, true)) {
//            val resultClass = param.result::class.java
//            val valueField = XposedHelpers.findField(resultClass, "value")
//            valueField.isAccessible = true
//            valueField.set(param.result, "0")

                            val result = param.result as Bundle
                            result.putString("value", "0") // Override the value to "0"

                            Log.d("processed ${param.method.name}($arg): ${param.result}")
                        } else {
                            Log.d("Skipping ${param.method.name}($arg) as preference is disabled")
                        }
                    }
                }
            }
        )
    }

    private fun DetectionMethod.SystemPropertiesMethod.doHook(
        prefs: XSharedPreferences,
        lpparam: LoadPackageParam,
    ) {
        return

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
                            param.result = getOverrideValue(methodName)
                            Log.d("processed ${param.method.name}($arg): ${param.result}")
                        }
                    }
                }
            )
        }
    }

    private fun notifyChange(lpparam: LoadPackageParam) {
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

                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d("Launched coroutine to handle notifications")

                        notification.collect { (change, callback) ->
                            Log.d("Received notification for change: $change")



                            callback()
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
