package top.ltfan.notdeveloper.xposed

import android.content.SharedPreferences
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.xposed.hook.hook

/**
 * Entry point of the module; the framework instantiates it for each module
 * generation in a process and notifies it about every package that is
 * loaded into that process.
 *
 * The module app communicates with the framework through the module
 * service (see [top.ltfan.notdeveloper.ModuleService]).
 */
class Module : XposedModule() {
    /**
     * Remote preferences shared with the module app; the framework keeps this
     * instance in sync with what the module app writes. A `null` value means
     * the framework delivers no remote preferences, which keeps every hook
     * enabled.
     */
    internal val preferences: SharedPreferences? by lazy {
        try {
            getRemotePreferences(RemotePreferencesGroup)
        } catch (e: Exception) {
            Log.e("remote preferences are unavailable, every hook stays enabled", e)
            null
        }
    }

    override fun onModuleLoaded(param: XposedModuleInterface.ModuleLoadedParam) {
        Log.d("loaded into ${param.processName} by $frameworkName $frameworkVersion (API $apiVersion)")
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        if (param.packageName.startsWith("android") || param.packageName.startsWith("com.android")) {
            return
        }

        Log.d("processing package ${param.packageName}")

        DetectionCategory.allMethods.forEach { method ->
            val hook = method.hook
            try {
                val installed = hook.install(param)
                if (installed > 0) {
                    Log.d("applied ${method.preferenceKey} to ${param.packageName}: $installed method(s)")
                } else {
                    Log.w("no method hooked for ${method.preferenceKey} in ${param.packageName}")
                }
            } catch (e: Exception) {
                Log.w(
                    "failed to apply ${method.preferenceKey} to ${param.packageName}: ${e.message}",
                    e
                )
            }
        }
    }
}
