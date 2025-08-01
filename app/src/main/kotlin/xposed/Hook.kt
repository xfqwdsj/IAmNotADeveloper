package top.ltfan.notdeveloper.xposed

import android.app.AndroidAppHelper
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.ProviderInfo
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
import top.ltfan.notdeveloper.broadcast.receiveChangeBroadcast
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod
import java.lang.reflect.Proxy
import kotlin.reflect.KMutableProperty0
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

//        registerSettingChangeNotifier(lpparam)
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

    Log.d("Processing SettingsMethod: $this")

    val settingsProviderClass = XposedHelpers.findClass(
        "com.android.providers.settings.SettingsProvider",
        lpparam.classLoader,
    )

    XposedBridge.hookAllMethods(
        settingsProviderClass, "packageValueForCallResult",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val arg = param.args[1] as String
                if (arg == settingKey) {
                    val uid = Binder.getCallingUid()
                    val packageName =
                        AndroidAppHelper.currentApplication().packageManager.getPackagesForUid(uid)
                            ?.firstOrNull() ?: return

//                        if (packageName != BuildConfig.APPLICATION_ID) {
//                            Log.d("Calling package is not the app itself, skipping hook $arg")
//                            return
//                        }

                    if (packageName.startsWith("android") || packageName.startsWith("com.android")) {
                        Log.d("Calling package is a system package, skipping hook $arg")
                        return
                    }

                    prefs.reload()

                    if (!prefs.getBoolean(preferenceKey, true)) {
                        Log.d("Skipping ${param.method.name}($arg) as preference is disabled")
                        return
                    }

                    Log.d("processing ${param.method.name} from $packageName with arg $arg")

                    val result = param.result as Bundle
                    result.putString("value", "0")

                    Log.d("processed ${param.method.name}($arg): ${param.result}")
                }
            }
        },
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
            },
        )
    }
}

private fun registerSettingChangeNotifier(lpparam: LoadPackageParam) {
    if (lpparam.packageName != "com.android.providers.settings") return

    val settingsProviderClass = XposedHelpers.findClass(
        "com.android.providers.settings.SettingsProvider",
        lpparam.classLoader,
    )

    XposedBridge.hookAllMethods(
        settingsProviderClass, "onCreate",
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
                                settingsProvider::class.java, "mSettingsRegistry"
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
        },
    )
}

@OptIn(ExperimentalTime::class)
private fun patchSystem(lpparam: LoadPackageParam) {
    if (lpparam.packageName != "android") return

    Log.d("Patching system service registry for NotDevService")

    val systemServiceRegistryClass = XposedHelpers.findClass(
        "android.app.SystemServiceRegistry",
        lpparam.classLoader,
    )

    val serviceFetcherInterface = XposedHelpers.findClass(
        "android.app.SystemServiceRegistry\$ServiceFetcher",
        lpparam.classLoader,
    )

    val application = AndroidAppHelper.currentApplication()

    val service = NotDevService { name, type ->
        Log.d("Received notification request for $name")
        val application = AndroidAppHelper.currentApplication()

        val uid = Binder.getCallingUid()
        val packageName =
            application.packageManager.getPackagesForUid(uid)?.firstOrNull() ?: run {
                Log.d("Calling package not found, skipping hook for $name")
                return@NotDevService
            }

        if (packageName != BuildConfig.APPLICATION_ID) {
            Log.d("Invalid calling package: $packageName, skipping hook for $name")
            return@NotDevService
        }

//                val providerHolder =
//                    XposedHelpers.getStaticObjectField(method.settingsClass, "sProviderHolder")

//                val provider = XposedHelpers.callMethod(
//                    providerHolder, "getProvider", application.contentResolver
//                ) as ContentProvider

        val userId = XposedHelpers.callStaticMethod(
            UserHandle::class.java, "getUserId", uid
        ) as Int

        val bundle = Bundle().apply {
            putInt(BundleExtraType, type)
            putInt("_user", userId)
        }

        val uri = "content://settings".toUri()
        application.contentResolver.call(
            uri,
            CallMethodNotify,
            name,
            bundle,
        )

        Log.d("Notified settings change for key: $name, type: $type, userId: $userId")
    }

    val fetcher = Proxy.newProxyInstance(
        serviceFetcherInterface.classLoader,
        arrayOf(serviceFetcherInterface),
    ) { _, method, args ->
        if (method.name == "getService") {
            Log.d("Intercepted getSystemService call for NotDevService")
            val context = args[0] as Context
            if (context.packageName != BuildConfig.APPLICATION_ID) {
                Log.d("Other package requested NotDevService, skipping hook")
                return@newProxyInstance null
            }
            return@newProxyInstance service.asBinder()
        }
    }

    XposedHelpers.callStaticMethod(
        systemServiceRegistryClass,
        "registerService",
        NotDevService::class.java.name,
        NotDevService::class.java,
        fetcher,
    )

//    val serviceManagerClass = XposedHelpers.findClass(
//        "android.os.ServiceManager",
//        lpparam.classLoader,
//    )

//    XposedHelpers.callStaticMethod(
//        serviceManagerClass,
//        "addService",
//        NotDevService::class.java.name,
//        service.asBinder(),
//    )

//    val provider = NotDevServiceProvider(service)
//    val iContentProvider = XposedHelpers.callMethod(
//        provider, "getIContentProvider",
//    )

//    provider.attachInfo(application, NotDevServiceProvider.info)

    val activityManagerServiceClass = XposedHelpers.findClass(
        "com.android.server.am.ActivityManagerService",
        lpparam.classLoader,
    )

    XposedBridge.hookAllMethods(
        activityManagerServiceClass, "systemReady",
//    XposedBridge.hookAllConstructors(
//        activityManagerServiceClass,
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val ams = param.thisObject
                Log.d("Got ActivityManagerService")

                val contextField = XposedHelpers.findField(
                    activityManagerServiceClass, "mContext"
                )
                val context = contextField.get(ams) as Context

//                Log.d("Context: $context")
//                runCatching { Log.d("Application: ${context.applicationInfo.packageName}") }
//                runCatching { Log.d("Package manager: ${context.packageManager}") }

                val contentProviderHelperField = XposedHelpers.findField(
                    activityManagerServiceClass, "mCpHelper"
                )
                val helper = contentProviderHelperField.get(ams)

                val providerMapField = XposedHelpers.findField(
                    helper::class.java, "mProviderMap"
                )
                val providerMap = providerMapField.get(helper)

                val contentProviderRecordClass = XposedHelpers.findClass(
                    "com.android.server.am.ContentProviderRecord",
                    lpparam.classLoader,
                )

//                val packageManager = XposedHelpers.callMethod(
//                    ams, "getPackageManager",
//                )

//                val applicationInfo = context.packageManager
//                    .getApplicationInfo(BuildConfig.APPLICATION_ID, 0)
                val applicationInfo = ApplicationInfo().apply {
                    packageName = "android"
                    uid = Process.SYSTEM_UID
                    flags = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
                }
                val providerInfo = NotDevServiceProvider.info.apply {
                    processName = "system_server"
                    this.applicationInfo = applicationInfo
                }

                val record = XposedHelpers.newInstance(
                    contentProviderRecordClass,
                    ams,
                    providerInfo,
                    applicationInfo,
                    ComponentName(application, NotDevServiceProvider::class.java),
                    true
                )

                val provider = NotDevServiceProvider(service)
                provider.attachInfo(context, providerInfo)

                val iContentProvider = XposedHelpers.callMethod(
                    provider, "getIContentProvider",
                )

                val providerField = XposedHelpers.findField(
                    contentProviderRecordClass, "provider"
                )
                providerField.set(record, iContentProvider)

                XposedHelpers.callMethod(
                    providerMap, "putProviderByName",
                    NotDevServiceProvider.uri.authority,
                    record,
                )

                Log.d("Registered NotDevServiceProvider with ActivityManagerService")
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
            var iPackageManagerUnhook: Set<Unhook>? = null

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

                val serviceField = XposedHelpers.findField(
                    helper::class.java, "mService"
                )
                val ams = serviceField.get(helper)

                val providerMapField = XposedHelpers.findField(
                    helper::class.java, "mProviderMap"
                )
                val providerMap = providerMapField.get(helper)

                val record = XposedHelpers.callMethod(
                    providerMap, "getProviderByName",
                    NotDevServiceProvider.uri.authority, 0
                )

                Log.d("Content provider record: $record")

                val info = XposedHelpers.getObjectField(record, "info") as ProviderInfo

                val processRecord = XposedHelpers.callMethod(
                    ams, "getRecordForAppLOSP", param.args[0]
                )

                Log.d("Process record: $processRecord")

//                val process = mService.startProcessLocked(
//                    cpi.processName, cpr.appInfo, false, 0,
//                    new HostingRecord(HostingRecord.HOSTING_TYPE_CONTENT_PROVIDER,
//                    new ComponentName(
//                            cpi.applicationInfo.packageName, cpi.name)),
//                Process.ZYGOTE_POLICY_FLAG_EMPTY, false, false)

                val appInfo = XposedHelpers.getObjectField(record, "appInfo")

                Log.d("App info: $appInfo")

                val hostingRecordClass = XposedHelpers.findClass(
                    "com.android.server.am.HostingRecord",
                    lpparam.classLoader,
                )
//                val hostingType = XposedHelpers.getStaticObjectField(
//                    hostingRecordClass, "HOSTING_TYPE_CONTENT_PROVIDER"
//                )
                val componentName = ComponentName(info.applicationInfo.packageName, info.name)
                val hostingRecord = XposedHelpers.newInstance(
                    hostingRecordClass,
                    "content provider",
                    componentName,
                )

                val flag = XposedHelpers.getStaticIntField(
                    Process::class.java, "ZYGOTE_POLICY_FLAG_EMPTY"
                )

                val appGlobalsClass = XposedHelpers.findClass(
                    "android.app.AppGlobals", lpparam.classLoader
                )
                val iPackageManager = XposedHelpers.callStaticMethod(
                    appGlobalsClass, "getPackageManager"
                )

//                val iPackageManagerClass = XposedHelpers.findClass(
//                    "android.content.pm.IPackageManager",
//                    lpparam.classLoader,
//                )

                Log.d("IPackageManager: ${iPackageManager::class.java.name}")

                iPackageManagerUnhook = XposedBridge.hookAllMethods(
                    iPackageManager::class.java, "resolveContentProvider",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (param.args[0] != NotDevServiceProvider.uri.authority) return
                            Log.d("Intercepted resolveContentProvider call for NotDevServiceProvider")
                            param.result = info
                        }
                    },
                )

                val startTime = Clock.System.now()
                val startTimeMs = startTime.toEpochMilliseconds()
                val startTimeNs =
                    startTime.epochSeconds * 1_000_000_000 + startTime.nanosecondsOfSecond

                val processList = XposedHelpers.getObjectField(ams, "mProcessList")

                val connection = XposedHelpers.callMethod(
                    helper, "incProviderCountLocked",
                    processRecord, record, param.args[2], param.args[3], param.args[4],
                    param.args[5], param.args[6], true, startTimeMs, processList, param.args[7],
                )

                Log.d("Connection: $connection")

                param.result = XposedHelpers.callMethod(
                    record, "newHolder",
                    connection, false,
                )

//
//                val resolveResult = XposedHelpers.callMethod(
//                    iPackageManager, "resolveContentProvider",
//                    NotDevServiceProvider.uri.authority, 0, param.args[3]
//                )
//
//                Log.d("Resolve result: $resolveResult")

//                val process = XposedHelpers.callMethod(
//                    ams, "startProcessLocked",
//                    info.processName,
//                    appInfo,
//                    false,
//                    0,
//                    hostingRecord,
//                    flag,
//                    false,
//                    false
//                )

//
//                val contentProviderRecordClass = XposedHelpers.findClass(
//                    "com.android.server.am.ContentProviderRecord",
//                    lpparam.classLoader,
//                )
//
//                val applicationInfo = ApplicationInfo().apply {
//                    packageName = "android"
//                    uid = Process.SYSTEM_UID
//                    flags = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
//                }
//                val providerInfo = NotDevServiceProvider.info.apply {
//                    this.applicationInfo = applicationInfo
//                }
//
//                val record = XposedHelpers.newInstance(
//                    contentProviderRecordClass,
//                    ams,
//                    providerInfo,
//                    applicationInfo,
//                    ComponentName(application, NotDevServiceProvider::class.java),
//                    true
//                )
//
//                val contentProviderHolderClass = XposedHelpers.findClass(
//                    "android.app.ContentProviderHolder",
//                    lpparam.classLoader,
//                )
//                val holder = XposedHelpers.newInstance(
//                    contentProviderHolderClass,
//                    NotDevServiceProvider.info.apply {
//                        applicationInfo = AndroidAppHelper.currentApplication()
//                            .packageManager.getApplicationInfo(
//                                BuildConfig.APPLICATION_ID, 0
//                            )
//                        Log.d("Application info: $applicationInfo")
//                    },
//                )
//
//                val providerField = XposedHelpers.findField(
//                    contentProviderHolderClass, "provider"
//                )
//
//                providerField.set(holder, iContentProvider)
//
//                param.result = holder
//
//                Log.d("Returning NotDevServiceProvider")
            }

            override fun afterHookedMethod(param: MethodHookParam?) {
                Log.d("Finished getContentProviderImpl hook")
                ::iPackageManagerUnhook.unhook()
            }

            fun KMutableProperty0<Set<Unhook>?>.unhook() {
                get()?.forEach { it.unhook() }
                set(null)
            }
        },
    )

//    XposedBridge.hookAllMethods(
//        systemServiceRegistryClass, "getSystemService",
//        object : XC_MethodHook() {
//            override fun beforeHookedMethod(param: MethodHookParam) {
//                val context = param.args[0] as Context
//                if (context.packageName != BuildConfig.APPLICATION_ID) return
//                val name = param.args[1] as String
//                if (name != NotDevService::class.java.name) {
//                    Log.d("NotDevService not requested, skipping hook for $name")
//                    return
//                }
//
//                Log.d("Intercepted request for NotDevService")
//
//                param.result = service
//            }
//        },
//    )

    Log.d("Patched system service registry for NotDevService")
}

private fun patchSettingsProvider(lpparam: LoadPackageParam) {
    if (lpparam.packageName != "com.android.providers.settings") return

    Log.d("Hooking into SettingsProvider for NotDevService")

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
                val packageName = AndroidAppHelper.currentApplication()
                    .packageManager.getPackagesForUid(uid)?.firstOrNull() ?: return
                if (packageName != BuildConfig.APPLICATION_ID && packageName != "android") {
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

                val settingsRegistryField = XposedHelpers.findField(
                    settingsProvider::class.java, "mSettingsRegistry"
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

                val key = XposedHelpers.callStaticMethod(
                    settingsStateClass, "makeKey", type, userId
                )

                notify.invoke(settingsRegistry, key, name)

                Log.d("Notified settings change for key: $key, setting: $name")

                param.result = Bundle()
            }
        },
    )

    Log.d("Patched SettingsProvider for NotDevService")

//    XposedBridge.hookAllMethods(
//        settingsProviderClass, "onCreate",
//        object : XC_MethodHook() {
//            override fun afterHookedMethod(param: MethodHookParam) {
//                Log.d("Got SettingsProvider")
//
////                val application = AndroidAppHelper.currentApplication()
//                NotDevService { method ->
//                    Log.d("Received notification request for ${method.preferenceKey}")
//
//                    when (method) {
//                        is DetectionMethod.SettingsMethod -> {
//                            val settingsProvider = param.thisObject
//
//                            val settingsRegistryField = XposedHelpers.findField(
//                                settingsProvider::class.java, "mSettingsRegistry"
//                            )
//                            val settingsRegistry = settingsRegistryField.get(settingsProvider)
//
//                            val notify = XposedHelpers.findMethodExact(
//                                settingsRegistry::class.java,
//                                "notifyForSettingsChange",
//                                Int::class.java,
//                                String::class.java,
//                            )
//
//                            val settingsStateClass = XposedHelpers.findClass(
//                                "com.android.providers.settings.SettingsState",
//                                lpparam.classLoader,
//                            )
//
//                            val userId = XposedHelpers.callStaticMethod(
//                                UserHandle::class.java,
//                                "myUserId",
//                            ) as Int
//
//                            val key = XposedHelpers.callStaticMethod(
//                                settingsStateClass,
//                                "makeKey",
//                                when (method.settingsClass) {
//                                    android.provider.Settings.Global::class.java -> 0
//                                    android.provider.Settings.System::class.java -> 1
//                                    android.provider.Settings.Secure::class.java -> 2
//                                    else -> return@NotDevService
//                                },
//                                userId,
//                            )
//
//                            notify.invoke(settingsRegistry, key, method.settingKey)
//
//                            Log.d("Notified settings change for key: $key, setting: ${method.settingKey}")
//                        }
//
//                        else -> Unit
//                    }
//                }
//
////                val intent = Intent(application, service::class.java)
////                application.startService(intent)
//
//                Log.d("Started NotDevService")
//            }
//        },
//    )
}

private fun DetectionMethod.hook(prefs: XSharedPreferences, lpparam: LoadPackageParam) {
    when (this) {
        is DetectionMethod.SettingsMethod -> doHook(prefs, lpparam)
        is DetectionMethod.SystemPropertiesMethod -> doHook(prefs, lpparam)
    }
}
