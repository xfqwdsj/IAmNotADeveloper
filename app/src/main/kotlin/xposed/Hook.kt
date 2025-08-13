package top.ltfan.notdeveloper.xposed

import android.app.AndroidAppHelper
import android.content.pm.ApplicationInfo
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.UserHandle
import androidx.annotation.Keep
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.log.callingPackageNotFoundWhen
import top.ltfan.notdeveloper.log.disabledHook
import top.ltfan.notdeveloper.log.invalidPackage
import top.ltfan.notdeveloper.log.notPreferredHook
import top.ltfan.notdeveloper.log.processing
import top.ltfan.notdeveloper.provider.DatabaseServiceProvider
import top.ltfan.notdeveloper.provider.SystemServiceProvider
import top.ltfan.notdeveloper.provider.getInterfaceOrNull
import top.ltfan.notdeveloper.service.BundleExtraType
import top.ltfan.notdeveloper.service.CallMethodNotify
import top.ltfan.notdeveloper.service.DatabaseService
import top.ltfan.notdeveloper.service.DatabaseServiceClient
import top.ltfan.notdeveloper.service.DatabaseServiceInterface
import top.ltfan.notdeveloper.service.SystemService
import top.ltfan.notdeveloper.service.client
import top.ltfan.notdeveloper.service.unwrap
import top.ltfan.notdeveloper.util.clearBinderCallingIdentity
import top.ltfan.notdeveloper.xposed.hook.RegisteredProvider
import top.ltfan.notdeveloper.xposed.hook.withContentProviderContext
import kotlin.reflect.jvm.javaMethod
import kotlin.time.ExperimentalTime

@Keep
class Hook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == BuildConfig.APPLICATION_ID) {
            XposedHelpers.findAndHookMethod(
                StatusProxy::class.java.name,
                lpparam.classLoader,
                StatusProxy::get.javaMethod?.name,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = true
                    }
                },
            )
        }

        withLpparamContext(lpparam) {
            patchSystem()
            patchSettingsProvider()

            DetectionCategory.allMethods.forEach { method ->
                try {
                    method.hook()
                } catch (e: Throwable) {
                    Log.e("Failed to apply hook for ${method::class.simpleName}: ${e.message}", e)
                }
            }
        }
    }
}

context(lpparam: XC_LoadPackage.LoadPackageParam)
private fun DetectionMethod.SettingsMethod.doHook() {
    if (lpparam.packageName != "com.android.providers.settings") return

    Log.processing("${DetectionMethod.SettingsMethod::class.qualifiedName}: $settingKey") {
        val settingsProviderClass = XposedHelpers.findClass(
            "com.android.providers.settings.SettingsProvider", lpparam.classLoader
        )

        XposedBridge.hookAllMethods(
            settingsProviderClass, "packageValueForCallResult",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val name = param.args[1] as String
                    if (name != settingKey) return

                    val packageManager = AndroidAppHelper.currentApplication().packageManager
                    val uid = Binder.getCallingUid()
                    val packageName =
                        packageManager.getPackagesForUid(uid)?.takeIf { it.size == 1 }?.first()
                            ?: run {
                                Log.debug callingPackageNotFoundWhen "${param.method.name}($name)"
                                return
                            }

                    val client = databaseServiceClient
                    val methods = DetectionMethod.SettingsMethod.fromSettingKey(name)

                    val userId = XposedHelpers.callStaticMethod(
                        UserHandle::class.java, "getUserId", uid
                    ) as Int

                    val isSet = runBlocking(Dispatchers.IO) {
                        client.isDetectionSet(packageName, userId, methods).all { it }
                    }
                    if (!isSet) {
                        Log notPreferredHook "${param.method.name}($name)"
                        return
                    }

                    val appInfo = XposedHelpers.callMethod(
                        packageManager, "getApplicationInfoAsUser",
                        packageName, 0, userId
                    ) as ApplicationInfo

                    val isSystem =
                        appInfo.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    if (isSystem) {
                        Log.i("Calling package $packageName of ${param.method.name}($name) is a system package")
                    }

                    Log.processing("${param.method.name}($name) from $packageName") {
                        val enabled = runBlocking(Dispatchers.IO) {
                            client.isDetectionEnabled(packageName, userId, methods).all { it }
                        }
                        if (!enabled) {
                            Log disabledHook "${param.method.name}($name)"
                            return
                        }

                        val result = param.result as Bundle
                        result.putString("value", "0")
                    }
                }
            },
        )
    }
}

context(lpparam: XC_LoadPackage.LoadPackageParam)
private fun DetectionMethod.SystemPropertiesMethod.doHook() {
    val packageName = lpparam.packageName
    if (packageName.startsWith("android") || packageName.startsWith("com.android")) return

    Log.processing("${DetectionMethod.SystemPropertiesMethod::class.qualifiedName}: $propertyKey") {
        val systemPropertiesClass = XposedHelpers.findClass(
            "android.os.SystemProperties", lpparam.classLoader
        )

        // TODO: Native hooks
        val methods = listOf("get", "getprop", "getBoolean", "getInt", "getLong")

        methods.forEach { methodName ->
            XposedBridge.hookAllMethods(
                systemPropertiesClass, methodName,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val name = param.args[0] as String
                        if (name != propertyKey) return

                        val client = databaseServiceClient
                        val methods = DetectionMethod.SystemPropertiesMethod.fromPropertyKey(name)

                        Log.processing("${param.method.name}($name) from $packageName") {
                            val userId = XposedHelpers.callStaticMethod(
                                UserHandle::class.java, "myUserId"
                            ) as Int

                            val enabled = runBlocking(Dispatchers.IO) {
                                client.isDetectionEnabled(packageName, userId, methods).all { it }
                            }
                            if (!enabled) {
                                Log disabledHook "${param.method.name}($name)"
                                return
                            }

                            param.result = getOverrideValue(methodName)
                            logAppendix = ", returning override value: ${param.result}"
                        }
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
context(lpparam: XC_LoadPackage.LoadPackageParam)
private fun patchSystem() {
    if (lpparam.packageName != "android") return

    Log.processing("system") {
        val service = SystemService(lpparam)

        val activityManagerServiceClass = XposedHelpers.findClass(
            "com.android.server.am.ActivityManagerService",
            lpparam.classLoader,
        )

        XposedBridge.hookAllMethods(
            activityManagerServiceClass, "getContentProvider",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val args = param.args
                    val argsOffset = when (args.size) {
                        4 -> 0
                        5 -> 1
                        else -> {
                            Log.debug.e("Unsupported Android version, args size: ${args.size}")
                            return
                        }
                    }
                    val name = args[1 + argsOffset] as String
                    if (!RegisteredProvider.entries.any { it.authority == name }) return
                    val callingUid = Binder.getCallingUid()
                    val callingPackage = when (args.size) {
                        4 -> {
                            AndroidAppHelper.currentApplication().packageManager
                                .getPackagesForUid(callingUid)?.firstOrNull() ?: run {
                                Log callingPackageNotFoundWhen "getContentProvider($name)"
                                return
                            }
                        }

                        5 -> args[1] as String
                        else -> return
                    }

                    val caller = args[0] // IApplicationThread
                    val userId = args[2 + argsOffset] as Int
                    val stable = args[3 + argsOffset] as Boolean

                    val ams = param.thisObject
                    val helper = try {
                        XposedHelpers.getObjectField(ams, "mCpHelper")
                    } catch (e: NoSuchFieldError) {
                        Log.d("mCpHelper field not found, using ActivityManagerService directly", e)
                        ams
                    }

                    withContentProviderContext(
                        ams,
                        helper,
                        caller,
                        callingPackage,
                        callingUid,
                        userId,
                        stable,
                        lpparam,
                        param,
                        name,
                    ) {
                        RegisteredProvider.SystemService(SystemServiceProvider(service))
                        RegisteredProvider.DatabaseService()
                    }
                }
            },
        )
    }
}

context(lpparam: XC_LoadPackage.LoadPackageParam)
private fun patchSettingsProvider() {
    if (lpparam.packageName != "com.android.providers.settings") return

    Log.processing("SettingsProvider") {
        val settingsProviderClass = XposedHelpers.findClass(
            "com.android.providers.settings.SettingsProvider",
            lpparam.classLoader,
        )

        XposedBridge.hookAllMethods(
            settingsProviderClass, "call",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val method = param.args[0] as String
                    if (method != CallMethodNotify) return
                    val uid = Binder.getCallingUid()
                    val packageNames =
                        AndroidAppHelper.currentApplication().packageManager.getPackagesForUid(uid)
                            ?: run {
                                Log.debug callingPackageNotFoundWhen "SettingsProvider.call($method)"
                                return
                            }
                    val valid = packageNames.any {
                        it == BuildConfig.APPLICATION_ID || it == "android"
                    }
                    if (!valid) {
                        val packageNames = packageNames.joinToString()
                        Log.debug invalidPackage packageNames skipping "hook for SettingsProvider.call"
                        return
                    }

                    val name = param.args[1] as String
                    val args = param.args[2] as Bundle
                    val type = args.getInt(BundleExtraType, 0)

                    val settingsProvider = param.thisObject

                    val userId = XposedHelpers.callStaticMethod(
                        settingsProvider::class.java, "getRequestingUserId", args
                    ) as Int

                    val settingsRegistry = XposedHelpers.getObjectField(
                        settingsProvider, "mSettingsRegistry"
                    )

                    val settingsStateClass = XposedHelpers.findClass(
                        "com.android.providers.settings.SettingsState",
                        lpparam.classLoader,
                    )

                    val key = XposedHelpers.callStaticMethod(
                        settingsStateClass, "makeKey", type, userId
                    )

                    XposedHelpers.callMethod(
                        settingsRegistry, "notifyForSettingsChange",
                        key, name,
                    )

                    Log.d("Notified settings change for key: $key, setting: $name")

                    param.result = Bundle()
                }
            },
        )
    }
}

context(lpparam: XC_LoadPackage.LoadPackageParam)
private fun DetectionMethod.hook() {
    when (this) {
        is DetectionMethod.SettingsMethod -> doHook()
        is DetectionMethod.SystemPropertiesMethod -> doHook()
    }
}

private inline fun <R> withLpparamContext(
    lpparam: XC_LoadPackage.LoadPackageParam,
    block: context(XC_LoadPackage.LoadPackageParam) () -> R,
) = with(lpparam, block)

@Suppress("ObjectPropertyName")
private var _databaseServiceClient: DatabaseServiceClient? = databaseServiceClient
private val databaseServiceClient: DatabaseServiceClient
    get() {
        val instance = _databaseServiceClient
        if (instance?.remote?.isBinderAlive == true) return instance

        return clearBinderCallingIdentity {
            runCatching {
                AndroidAppHelper.currentApplication().contentResolver
                    .getInterfaceOrNull(DatabaseServiceProvider) {
                        it.linkToDeath(
                            object : IBinder.DeathRecipient {
                                override fun binderDied() {
                                    Log.w("DatabaseService binder died, reconnecting")
                                    it.unlinkToDeath(this, 0)
                                    _databaseServiceClient = databaseServiceClient
                                }
                            },
                            0,
                        )
                        it.unwrap(DatabaseServiceInterface::class).client
                    }
            }.getOrNull()
        } ?: run {
            Log.w("${DatabaseService::class.qualifiedName} not found, using stub implementation")
            DatabaseServiceClient
        }
    }
