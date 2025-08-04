package top.ltfan.notdeveloper.xposed

import android.app.AndroidAppHelper
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Binder
import android.os.Bundle
import android.os.UserHandle
import androidx.annotation.Keep
import androidx.core.net.toUri
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.data.SystemDataDir
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.provider.PackageSettingsDaoProvider
import top.ltfan.notdeveloper.provider.getInterfaceOrNull
import top.ltfan.notdeveloper.service.data.IPackageSettingsDao
import top.ltfan.notdeveloper.xposed.hook.RegisteredProvider
import top.ltfan.notdeveloper.xposed.hook.withContentProviderContext
import java.io.File
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

            val remoteDao = AndroidAppHelper.currentApplication().contentResolver
                .getInterfaceOrNull(PackageSettingsDaoProvider) {
                    PackageSettingsDaoClient(IPackageSettingsDao.Stub.asInterface(it))
                }
            val dao = remoteDao ?: run {
                Log.w("PackageSettingsDao not found, using stub implementation")
                PackageSettingsDaoClient
            }

            DetectionCategory.allMethods.forEach { method ->
                try {
                    method.hook(dao)
                } catch (e: Throwable) {
                    Log.e("Failed to apply hook for ${method::class.simpleName}: ${e.message}", e)
                }
            }
        }
    }
}

context(lpparam: XC_LoadPackage.LoadPackageParam, dao: PackageSettingsDaoClientInterface)
private fun DetectionMethod.SettingsMethod.doHook() {
    if (lpparam.packageName != "com.android.providers.settings") return

    Log.d("Processing SettingsMethod: $settingKey")

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
                    packageManager.getPackagesForUid(uid)?.firstOrNull() ?: run {
                        Log.debug.e("Calling package not found")
                        return
                    }
                val userId = XposedHelpers.callStaticMethod(
                    UserHandle::class.java, "getUserId", uid
                ) as Int

                val appInfo = XposedHelpers.callMethod(
                    packageManager, "getApplicationInfoAsUser",
                    packageName, 0, userId
                ) as ApplicationInfo

                val methods = DetectionMethod.SettingsMethod.fromSettingKey(name)

                val isSet = dao.isDetectionSet(packageName, userId, methods).all { it }
                if (!isSet) {
                    Log.d("Skipping ${param.method.name}($name) as injection preferences is not set")
                    return
                }

                val isSystem =
                    appInfo.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                if (isSystem) {
                    Log.i("Calling package $packageName of ${param.method.name}($name) is a system package")
                }

                Log.d("Processing ${param.method.name}($name) from $packageName")

                val enabled = dao.isDetectionEnabled(packageName, userId, methods).all { it }
                if (!enabled) {
                    Log.d("Skipping ${param.method.name}($name) as injection is disabled")
                    return
                }

                val result = param.result as Bundle
                result.putString("value", "0")

                Log.d("Processed ${param.method.name}($name) from $packageName")
            }
        },
    )

    Log.d("Processed SettingsMethod: $settingKey")
}

context(lpparam: XC_LoadPackage.LoadPackageParam, dao: PackageSettingsDaoClientInterface)
private fun DetectionMethod.SystemPropertiesMethod.doHook() {
    val packageName = lpparam.packageName
    if (packageName.startsWith("android") || packageName.startsWith("com.android")) return

    Log.d("Processing SystemPropertiesMethod: $propertyKey")

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

                    val userId = XposedHelpers.callStaticMethod(
                        UserHandle::class.java, "myUserId"
                    ) as Int

                    val methods = DetectionMethod.SystemPropertiesMethod.fromPropertyKey(name)

                    Log.d("Processing ${param.method.name}($name) from $packageName")

                    val enabled = dao.isDetectionEnabled(packageName, userId, methods).all { it }
                    if (!enabled) {
                        Log.d("Skipping ${param.method.name}($name) as injection is disabled")
                        return
                    }

                    param.result = getOverrideValue(methodName)
                    Log.d("Processed ${param.method.name}($name) from $packageName, returning override value: ${param.result}")
                }
            },
        )
    }

    Log.d("Processed SystemPropertiesMethod: $propertyKey")
}

@OptIn(ExperimentalTime::class)
context(lpparam: XC_LoadPackage.LoadPackageParam) private fun patchSystem() {
    if (lpparam.packageName != "android") return

    Log.d("Patching system for NotDevService")

    val service = NotDevService {
        notify { name, type ->
            val application = AndroidAppHelper.currentApplication()
            val uid = Binder.getCallingUid()
            val packageName =
                application.packageManager.getPackagesForUid(uid)?.firstOrNull() ?: run {
                    Log.debug.e("Calling package not found")
                    return@notify
                }

            if (packageName != BuildConfig.APPLICATION_ID) {
                Log.debug.e("Invalid package $packageName, skipping notification for $name")
                return@notify
            }

            Log.d("Received notification request for $name")

            val userId = XposedHelpers.callStaticMethod(
                UserHandle::class.java, "getUserId", uid
            ) as Int

            val bundle = Bundle().apply {
                putInt(BundleExtraType, type)
                putInt("_user", userId)
            }

            application.contentResolver.call(
                "content://settings".toUri(),
                CallMethodNotify,
                name,
                bundle,
            )

            Log.d("Requested notification for $name with type $type from package $packageName, user ID: $userId")
        }
    }

    val contextImplClass = XposedHelpers.findClass(
        "android.app.ContextImpl",
        lpparam.classLoader,
    )

    XposedBridge.hookAllMethods(
        contextImplClass, "getDataDir",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val context = param.thisObject as Context
                if (context.packageName != "android") return

                Log.d("Providing data directory for system_server")
                val dir = File(SystemDataDir)
                if (!dir.exists()) {
                    Log.d("Creating data directory: $dir")
                    dir.mkdirs()
                } else if (!dir.isDirectory) {
                    Log.w("Data directory is not a directory: $dir")
                    return
                }

                param.result = dir
                Log.d("Provided data directory: $dir")
            }
        },
    )

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
                        AndroidAppHelper.currentApplication().packageManager.getPackagesForUid(
                            callingUid
                        )?.firstOrNull() ?: run {
                            Log.debug.e("Calling package not found")
                            return
                        }
                    }

                    5 -> args[1] as String
                    else -> return
                }
                if (callingPackage != BuildConfig.APPLICATION_ID) {
                    Log.debug.e("Invalid package $callingPackage requesting NotDevServiceProvider")
                    return
                }

                Log.d("Building NotDevServiceProvider for $callingPackage")

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
                ) {
                    RegisteredProvider.NotDevService(NotDevServiceProvider(service))
                    RegisteredProvider.PackageSettingsDao()
                }
            }
        },
    )

    Log.d("Patched system for NotDevService")
}

context(lpparam: XC_LoadPackage.LoadPackageParam) private fun patchSettingsProvider() {
    if (lpparam.packageName != "com.android.providers.settings") return

    Log.d("Patching SettingsProvider")

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
                            Log.debug.e("Calling package not found")
                            return
                        }
                val valid = packageNames.any {
                    it == BuildConfig.APPLICATION_ID || it == "android"
                }
                if (!valid) {
                    val string = packageNames.toString()
                    Log.debug.d("Invalid calling package: $string")
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

    Log.d("Patched SettingsProvider")
}

context(lpparam: XC_LoadPackage.LoadPackageParam) private fun DetectionMethod.hook(dao: PackageSettingsDaoClientInterface) {
    withDaoContext(dao) {
        when (this) {
            is DetectionMethod.SettingsMethod -> doHook()
            is DetectionMethod.SystemPropertiesMethod -> doHook()
        }
    }
}

private inline fun <R> withLpparamContext(
    lpparam: XC_LoadPackage.LoadPackageParam,
    block: context(XC_LoadPackage.LoadPackageParam) () -> R,
) = with(lpparam, block)

private inline fun <R> withDaoContext(
    dao: PackageSettingsDaoClientInterface,
    block: context(PackageSettingsDaoClientInterface) () -> R,
) = with(dao, block)
