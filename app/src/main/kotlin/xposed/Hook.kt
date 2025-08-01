package top.ltfan.notdeveloper.xposed

import android.app.AndroidAppHelper
import android.content.ComponentName
import android.content.pm.ApplicationInfo
import android.os.Binder
import android.os.Bundle
import android.os.Process
import android.os.UserHandle
import androidx.annotation.Keep
import androidx.core.net.toUri
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod
import kotlin.reflect.jvm.javaMethod
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Keep
class Hook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
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

        patchSystem(lpparam)
        patchSettingsProvider(lpparam)

        val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID)
        DetectionCategory.allMethods.forEach { method ->
            try {
                method.hook(prefs, lpparam)
            } catch (e: Throwable) {
                Log.e("Failed to apply hook for ${method::class.simpleName}: ${e.message}", e)
            }
        }
    }
}

private fun DetectionMethod.SettingsMethod.doHook(
    prefs: XSharedPreferences, lpparam: LoadPackageParam
) {
    if (lpparam.packageName != "com.android.providers.settings") return

    Log.d("Processing SettingsMethod: ${this.settingKey}")

    val settingsProviderClass = XposedHelpers.findClass(
        "com.android.providers.settings.SettingsProvider", lpparam.classLoader
    )

    XposedBridge.hookAllMethods(
        settingsProviderClass, "packageValueForCallResult",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val name = param.args[1] as String
                if (name == settingKey) {
                    val uid = Binder.getCallingUid()
                    val packageName =
                        AndroidAppHelper.currentApplication().packageManager.getPackagesForUid(uid)
                            ?.firstOrNull() ?: return

                    // TODO: Package checks
                    if (packageName.startsWith("android") || packageName.startsWith("com.android")) {
                        Log.d("Calling package is a system package, skipping hook $name")
                        return
                    }

                    prefs.reload()

                    Log.d("Processing ${param.method.name}($name) from $packageName")

                    // TODO: Package-specific checks
                    if (!prefs.getBoolean(preferenceKey, true)) {
                        Log.d("Skipping ${param.method.name}($name) as preference is disabled")
                        return
                    }

                    val result = param.result as Bundle
                    result.putString("value", "0")

                    Log.d("Processed ${param.method.name}($name) from $packageName")
                }
            }
        },
    )

    Log.d("Processed SettingsMethod: ${this.settingKey}")
}

private fun DetectionMethod.SystemPropertiesMethod.doHook(
    prefs: XSharedPreferences,
    lpparam: LoadPackageParam,
) {
    val packageName = lpparam.packageName
    if (packageName.startsWith("android") || packageName.startsWith("com.android")) return

    Log.d("Processing SystemPropertiesMethod: ${this.propertyKey}")

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

                    prefs.reload()

                    Log.d("Processing ${param.method.name}($name) from $packageName")

                    if (!prefs.getBoolean(preferenceKey, true)) {
                        Log.d("Skipping ${param.method.name}($name) as preference is disabled")
                        return
                    }

                    if (name == propertyKey) {
                        param.result = getOverrideValue(methodName)
                        Log.d("Processed ${param.method.name}($name) from $packageName, returning override value: ${param.result}")
                    }
                }
            },
        )
    }

    Log.d("Processed SystemPropertiesMethod: ${this.propertyKey}")
}

@OptIn(ExperimentalTime::class)
private fun patchSystem(lpparam: LoadPackageParam) {
    if (lpparam.packageName != "android") return

    Log.d("Patching system for NotDevService")

    val service = NotDevService {
        notify { name, type ->
            Log.d("Received notification request for $name")

            val application = AndroidAppHelper.currentApplication()
            val uid = Binder.getCallingUid()
            val packageName = application.packageManager.getPackagesForUid(uid)?.firstOrNull() ?: run {
                Log.d("Calling package not found, skipping notification for $name")
                return@notify
            }

            if (packageName != BuildConfig.APPLICATION_ID) {
                Log.d("Invalid package $packageName, skipping notification for $name")
                return@notify
            }

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

    val activityManagerServiceClass = XposedHelpers.findClass(
        "com.android.server.am.ActivityManagerService",
        lpparam.classLoader,
    )

    XposedBridge.hookAllMethods(
        activityManagerServiceClass, "systemReady",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val ams = param.thisObject
                Log.d("Got ActivityManagerService, registering provider")

                val helper = XposedHelpers.getObjectField(ams, "mCpHelper")
                val providerMap = XposedHelpers.getObjectField(helper, "mProviderMap")

                val contentProviderRecordClass = XposedHelpers.findClass(
                    "com.android.server.am.ContentProviderRecord",
                    lpparam.classLoader,
                )

                val applicationInfo = ApplicationInfo().apply {
                    packageName = "android"
                    uid = Process.SYSTEM_UID
                    flags = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
                }
                val providerInfo = NotDevServiceProvider.info.apply {
                    processName = "system_server"
                    this.applicationInfo = applicationInfo
                }

                val application = AndroidAppHelper.currentApplication()
                val record = XposedHelpers.newInstance(
                    contentProviderRecordClass,
                    ams,
                    providerInfo,
                    applicationInfo,
                    ComponentName(application, NotDevServiceProvider::class.java),
                    true
                )

                val provider = NotDevServiceProvider(service)
                provider.attachInfo(application, providerInfo)

                val iContentProvider = XposedHelpers.callMethod(
                    provider, "getIContentProvider",
                )

                XposedHelpers.setObjectField(record, "provider", iContentProvider)

                XposedHelpers.callMethod(
                    providerMap, "putProviderByName",
                    NotDevServiceProvider.uri.authority,
                    record,
                )

                Log.d("Registered NotDevServiceProvider")
            }
        },
    )

    val contentProviderHelperClass = XposedHelpers.findClass(
        "com.android.server.am.ContentProviderHelper",
        lpparam.classLoader,
    )

    XposedBridge.hookAllMethods(
        contentProviderHelperClass, "getContentProviderImpl",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val name = param.args[1] as String
                if (name != NotDevServiceProvider.uri.authority) return
                val callingPackage = param.args[4] as String
                if (callingPackage != BuildConfig.APPLICATION_ID) {
                    Log.d("Invalid package $callingPackage requesting NotDevServiceProvider, returning null")
                    param.result = null
                }

                Log.d("Intercepted request for NotDevServiceProvider")

                val helper = param.thisObject

                val ams = XposedHelpers.getObjectField(helper, "mService")
                val providerMap = XposedHelpers.getObjectField(helper, "mProviderMap")

                val record = XposedHelpers.callMethod(
                    providerMap, "getProviderByName", NotDevServiceProvider.uri.authority, 0
                )

                val processRecord = XposedHelpers.callMethod(
                    ams, "getRecordForAppLOSP", param.args[0]
                )

                val startTime = Clock.System.now()
                val startTimeMs = startTime.toEpochMilliseconds()

                val processList = XposedHelpers.getObjectField(ams, "mProcessList")

                val connection = XposedHelpers.callMethod(
                    helper, "incProviderCountLocked",
                    processRecord, record, param.args[2], param.args[3], param.args[4],
                    param.args[5], param.args[6], true, startTimeMs, processList, param.args[7],
                )

                param.result = XposedHelpers.callMethod(
                    record, "newHolder",
                    connection, false,
                )
            }
        },
    )

    Log.d("Patched system for NotDevService")
}

private fun patchSettingsProvider(lpparam: LoadPackageParam) {
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
                val packageName =
                    AndroidAppHelper.currentApplication().packageManager
                        .getPackagesForUid(uid) ?: return
                val valid = packageName.any {
                    it == BuildConfig.APPLICATION_ID || it == "android"
                }
                if (valid) {
                    Log.d("Invalid calling package: $packageName, skipping hook")
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

private fun DetectionMethod.hook(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
    when (this) {
        is DetectionMethod.SettingsMethod -> doHook(prefs, lpparam)
        is DetectionMethod.SystemPropertiesMethod -> doHook(prefs, lpparam)
    }
}
