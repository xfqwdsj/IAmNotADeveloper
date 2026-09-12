package top.ltfan.notdeveloper.xposed.hook

import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import top.ltfan.notdeveloper.xposed.Log
import top.ltfan.notdeveloper.xposed.Module
import java.lang.reflect.Executable

/**
 * How a system property override reaches the target process.
 *
 * The default [JvmPropertyBackend] hooks the `SystemProperties` getters.
 * A native backend can override the property's final value instead, which
 * also covers readers that bypass the Java getters.
 */
interface PropertyBackend {
    fun interceptor(
        executable: Executable,
        propertyKey: String,
        overrides: Map<String, Any>,
        isEnabled: () -> Boolean,
    ): Hooker

    context(module: Module)
    fun install(
        param: PackageReadyParam,
        propertyKey: String,
        overrides: Map<String, Any>,
        isEnabled: () -> Boolean,
    ): Int
}

/** Registry of the active [PropertyBackend]. */
object PropertyBackends {
    var backend: PropertyBackend = JvmPropertyBackend
}

/** Overrides the `SystemProperties` getters that read a given key. */
object JvmPropertyBackend : PropertyBackend {
    private const val SYSTEM_PROPERTIES_CLASS_NAME = "android.os.SystemProperties"

    override fun interceptor(
        executable: Executable,
        propertyKey: String,
        overrides: Map<String, Any>,
        isEnabled: () -> Boolean,
    ): Hooker {
        val override = overrides[executable.name]
        return Hooker { chain ->
            val key = chain.getArg(0) as String
            if (isEnabled() && key == propertyKey) {
                Log.Android.d("${executable.name}($key) reported as $override")
                override
            } else {
                chain.proceed()
            }
        }
    }

    context(module: Module)
    override fun install(
        param: PackageReadyParam,
        propertyKey: String,
        overrides: Map<String, Any>,
        isEnabled: () -> Boolean,
    ): Int {
        val systemProperties = try {
            Class.forName(SYSTEM_PROPERTIES_CLASS_NAME, false, param.classLoader)
        } catch (e: ClassNotFoundException) {
            Log.w("cannot find $SYSTEM_PROPERTIES_CLASS_NAME", e)
            return 0
        }

        var installed = 0
        overrides.keys.forEach { methodName ->
            installed += installAllMethods(systemProperties, methodName) {
                interceptor(it, propertyKey, overrides, isEnabled)
            }
        }
        return installed
    }
}
