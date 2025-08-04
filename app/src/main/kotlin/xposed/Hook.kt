package top.ltfan.notdeveloper.xposed

import android.app.AndroidAppHelper
import android.content.ComponentName
import android.content.Context
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
import top.ltfan.notdeveloper.data.SystemDataDir
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod
import java.io.File
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
                if (!prefs.getBoolean(this@doHook.name, true)) {
                    Log.d("Skipping ${param.method.name}($name) as preference is disabled")
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

private fun DetectionMethod.SystemPropertiesMethod.doHook(
    prefs: XSharedPreferences,
    lpparam: LoadPackageParam,
) {
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

                    prefs.reload()

                    Log.d("Processing ${param.method.name}($name) from $packageName")

                    if (!prefs.getBoolean(this@doHook.name, true)) {
                        Log.d("Skipping ${param.method.name}($name) as preference is disabled")
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
private fun patchSystem(lpparam: LoadPackageParam) {
    if (lpparam.packageName != "android") return

    Log.d("Patching system for NotDevService")

    val service = NotDevService {
        notify { name, type ->
            Log.d("Received notification request for $name")

            val application = AndroidAppHelper.currentApplication()
            val uid = Binder.getCallingUid()
            val packageName =
                application.packageManager.getPackagesForUid(uid)?.firstOrNull() ?: run {
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
                    4 -> 1
                    5 -> 0
                    else -> {
                        Log.e("Unsupported Android version, args size: ${args.size}")
                        return
                    }
                }
                val name = args[1 + argsOffset] as String
                if (name != NotDevServiceProvider.uri.authority) return
                val callingUid = Binder.getCallingUid()
                val callingPackage = when (args.size) {
                    4 -> {
                        AndroidAppHelper.currentApplication().packageManager
                            .getPackagesForUid(callingUid)?.firstOrNull() ?: run {
                            Log.e("Calling package not found")
                            return
                        }
                    }

                    5 -> args[1] as String
                    else -> return
                }
                if (callingPackage != BuildConfig.APPLICATION_ID) {
                    Log.d("Invalid package $callingPackage requesting NotDevServiceProvider")
                    return
                }

                val caller = args[0] // IApplicationThread
                val userId = args[2 + argsOffset] as Int
                val stable = args[3 + argsOffset] as Boolean

                val ams = param.thisObject
                val helper = try {
                    XposedHelpers.getObjectField(ams, "mCpHelper")
                } catch (e: NoSuchFieldError) {
                    Log.w("mCpHelper field not found, using ActivityManagerService directly", e)
                    ams
                }

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

                var processRecord: Any? = null

                try {
                    processRecord = XposedHelpers.callMethod(
                        ams, "getRecordForAppLOSP", caller
                    )
                } catch (e: NoSuchMethodError) {
                    Log.w("getRecordForAppLOSP method not found", e)
                }

                if (processRecord == null) {
                    try {
                        processRecord = XposedHelpers.callMethod(
                            ams, "getRecordForAppLocked", caller
                        )
                    } catch (e: Throwable) {
                        Log.w("getRecordForAppLocked method not found", e)
                    }
                }

                if (processRecord == null) {
                    Log.e("Failed to get process record for NotDevServiceProvider")
                    return
                }

                val startTime = Clock.System.now()
                val startTimeMs = startTime.toEpochMilliseconds()
                val processList = XposedHelpers.getObjectField(ams, "mProcessList")

                var connection: Any? = null

                try {
                    // Since https://cs.android.com/android/_/android/platform/frameworks/base/+/main:services/core/java/com/android/server/am/ContentProviderHelper.java;drc=eaa7835d03348af38535ce6a835408a63cee7b87;bpv=1;bpt=0;l=1287
                    connection = XposedHelpers.callMethod(
                        helper, "incProviderCountLocked",
                        processRecord, record, null, callingUid, callingPackage,
                        null, stable, true, startTimeMs, processList, userId,
                    )
                } catch (e: NoSuchMethodError) {
                    Log.d("Failed to get provider connection (1)", e)
                }

                if (connection == null) {
                    try {
                        // Since https://cs.android.com/android/_/android/platform/frameworks/base/+/main:services/core/java/com/android/server/am/ContentProviderHelper.java;drc=1109a0c5446c311c2fb2c97b5ee6253cd624e0e3;bpv=1;bpt=0;l=163
                        connection = XposedHelpers.callMethod(
                            helper, "incProviderCountLocked",
                            processRecord, record, null, callingUid, callingPackage,
                            null, stable, true, startTimeMs, processList,
                        )
                    } catch (e: NoSuchMethodError) {
                        Log.d("Failed to get provider connection (2)", e)
                    }
                }

                if (connection == null) {
                    try {
                        // Since https://cs.android.com/android/_/android/platform/frameworks/base/+/main:services/core/java/com/android/server/am/ActivityManagerService.java;drf=services%2Fjava%2Fcom%2Fandroid%2Fserver%2Fam%2FActivityManagerService.java;drc=20e809870d8ac1e5b848f2daf51b2272ef89bdfc;bpv=1;bpt=0;l=6161
                        XposedHelpers.callMethod(
                            ams, "incProviderCountLocked",
                            processRecord, record, null, stable,
                        )
                    } catch (e: NoSuchMethodError) {
                        Log.d("Failed to get provider connection (3)", e)
                    }
                }

                if (connection == null) {
                    Log.e("Failed to get connection for NotDevServiceProvider, returning null")
                    param.result = null
                    return
                } else {
                    Log.d("Got connection for NotDevServiceProvider")
                }

                param.result = XposedHelpers.callMethod(
                    record, "newHolder",
                    connection, false,
                )

                Log.d("Returning NotDevServiceProvider holder for $callingPackage")
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
                val packageNames =
                    AndroidAppHelper.currentApplication().packageManager
                        .getPackagesForUid(uid) ?: return
                val valid = packageNames.any {
                    it == BuildConfig.APPLICATION_ID || it == "android"
                }
                if (!valid) {
                    val string = packageNames.toString()
                    Log.d("Invalid calling package: $string, skipping hook")
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
