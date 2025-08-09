package top.ltfan.notdeveloper.provider

import android.app.AndroidAppHelper
import android.content.ComponentName
import android.content.ContentProvider
import android.content.pm.ApplicationInfo
import android.content.pm.ProviderInfo
import android.os.IBinder
import android.os.Process
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.service.SystemServiceInterface
import top.ltfan.notdeveloper.service.wrap
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.log.invalidPackage
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SystemServiceProvider(service: SystemServiceInterface) : BinderProvider() {
    override val binder: IBinder = service.wrap()

    companion object : BinderProvider.Companion {
        override val authority: String = SystemServiceProvider::class.java.name
        val info = ProviderInfo().apply {
            authority = Companion.authority
            name = Companion.authority
            exported = true
            grantUriPermissions = true
        }

        @OptIn(ExperimentalTime::class)
        fun patch(
            provider: ContentProvider,
            ams: Any,
            helper: Any,
            caller: Any,
            callingPackage: String,
            callingUid: Int,
            userId: Int,
            stable: Boolean,
            lpparam: XC_LoadPackage.LoadPackageParam,
            param: XC_MethodHook.MethodHookParam,
        ) {
            if (callingPackage != BuildConfig.APPLICATION_ID) {
                Log invalidPackage callingPackage requesting SystemServiceProvider::class.qualifiedName
                return
            }

            Log.d("Building SystemServiceProvider for $callingPackage")

            val contentProviderRecordClass = XposedHelpers.findClass(
                "com.android.server.am.ContentProviderRecord",
                lpparam.classLoader,
            )

            val applicationInfo = ApplicationInfo().apply {
                packageName = "android"
                uid = Process.SYSTEM_UID
                flags = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
            }
            val providerInfo = info.apply {
                processName = "system_server"
                this.applicationInfo = applicationInfo
            }

            val application = AndroidAppHelper.currentApplication()
            val record = XposedHelpers.newInstance(
                contentProviderRecordClass,
                ams,
                providerInfo,
                applicationInfo,
                ComponentName(application, SystemServiceProvider::class.java),
                true
            )

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
                Log.e("Failed to get process record for SystemServiceProvider")
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
                Log.w("Failed to get provider connection (1)", e)
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
                    Log.w("Failed to get provider connection (2)", e)
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
                    Log.w("Failed to get provider connection (3)", e)
                }
            }

            if (connection == null) {
                Log.e("Failed to get connection for SystemServiceProvider")
                return
            }

            Log.d("Got connection for SystemServiceProvider")

            param.result = XposedHelpers.callMethod(
                record, "newHolder",
                connection, false,
            )

            Log.d("Returning SystemServiceProvider holder for $callingPackage")
        }
    }
}
