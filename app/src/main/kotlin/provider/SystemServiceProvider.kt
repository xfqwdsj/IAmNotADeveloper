package top.ltfan.notdeveloper.provider

import android.content.ComponentName
import android.content.ContentProvider
import android.content.pm.ApplicationInfo
import android.content.pm.ProviderInfo
import android.os.IBinder
import android.os.Process
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.log.invalidPackage
import top.ltfan.notdeveloper.service.SystemServiceInterface
import top.ltfan.notdeveloper.service.wrap
import top.ltfan.notdeveloper.util.Reflect
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

        /**
         * Builds and returns the [ContentProviderHolder] that the system server
         * should hand to the module app for [authority], or `null` when the
         * caller must not receive it.
         */
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
            classLoader: ClassLoader,
        ): Any? {
            if (callingPackage != BuildConfig.APPLICATION_ID) {
                Log invalidPackage callingPackage requesting SystemServiceProvider::class.qualifiedName
                return null
            }

            Log.d("Building SystemServiceProvider for $callingPackage")

            val contentProviderRecordClass = Reflect.findClass(
                "com.android.server.am.ContentProviderRecord",
                classLoader,
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

            val application = Reflect.currentApplication()
            val record = Reflect.newInstance(
                contentProviderRecordClass,
                ams,
                providerInfo,
                applicationInfo,
                ComponentName(application, SystemServiceProvider::class.java),
                true,
            )

            provider.attachInfo(application, providerInfo)
            val iContentProvider = Reflect.callMethod(provider, "getIContentProvider")
            Reflect.setObjectField(record, "provider", iContentProvider)

            var processRecord: Any? = null

            try {
                processRecord = Reflect.callMethod(ams, "getRecordForAppLOSP", caller)
            } catch (e: Throwable) {
                Log.w("getRecordForAppLOSP method not found", e)
            }

            if (processRecord == null) {
                try {
                    processRecord = Reflect.callMethod(ams, "getRecordForAppLocked", caller)
                } catch (e: Throwable) {
                    Log.w("getRecordForAppLocked method not found", e)
                }
            }

            if (processRecord == null) {
                Log.e("Failed to get process record for SystemServiceProvider")
                return null
            }

            val startTimeMs = Clock.System.now().toEpochMilliseconds()
            val processList = Reflect.getObjectField(ams, "mProcessList")

            var connection: Any? = null

            try {
                // ContentProviderHelper.incProviderCountLocked with userId (Android 15+).
                connection = Reflect.callMethod(
                    helper, "incProviderCountLocked",
                    processRecord, record, null, callingUid, callingPackage,
                    null, stable, true, startTimeMs, processList, userId,
                )
            } catch (e: Throwable) {
                Log.w("Failed to get provider connection (1)", e)
            }

            if (connection == null) {
                try {
                    // ContentProviderHelper.incProviderCountLocked without userId.
                    connection = Reflect.callMethod(
                        helper, "incProviderCountLocked",
                        processRecord, record, null, callingUid, callingPackage,
                        null, stable, true, startTimeMs, processList,
                    )
                } catch (e: Throwable) {
                    Log.w("Failed to get provider connection (2)", e)
                }
            }

            if (connection == null) {
                try {
                    // ActivityManagerService.incProviderCountLocked (legacy).
                    connection = Reflect.callMethod(
                        ams, "incProviderCountLocked",
                        processRecord, record, null, stable,
                    )
                } catch (e: Throwable) {
                    Log.w("Failed to get provider connection (3)", e)
                }
            }

            if (connection == null) {
                Log.e("Failed to get connection for SystemServiceProvider")
                return null
            }

            Log.d("Got connection for SystemServiceProvider")

            return Reflect.callMethod(record, "newHolder", connection, false).also {
                Log.d("Returning SystemServiceProvider holder for $callingPackage")
            }
        }
    }
}
