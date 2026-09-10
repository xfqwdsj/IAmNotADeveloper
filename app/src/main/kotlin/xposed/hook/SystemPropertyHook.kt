package top.ltfan.notdeveloper.xposed.hook

import android.annotation.SuppressLint
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import top.ltfan.notdeveloper.xposed.Log
import top.ltfan.notdeveloper.xposed.Module
import java.lang.reflect.Executable

/**
 * Hides a system property by overriding the result of every
 * `android.os.SystemProperties` getter that reads it.
 *
 * [overrides] maps the name of a `SystemProperties` method to the value it
 * has to report instead; its keys also decide which methods are hooked.
 */
class SystemPropertyHook(
    private val propertyKey: String,
    private val preferenceKey: String,
    private val overrides: Map<String, Any>,
) : Hook {
    context(module: Module)
    override fun install(param: PackageReadyParam): Int {
        val systemProperties = try {
            @SuppressLint("PrivateApi")
            Class.forName(SYSTEM_PROPERTIES_CLASS_NAME, false, param.classLoader)
        } catch (e: ClassNotFoundException) {
            Log.w("cannot find $SYSTEM_PROPERTIES_CLASS_NAME", e)
            return 0
        }

        var installed = 0
        overrides.keys.forEach { methodName ->
            installed += installAllMethods(systemProperties, methodName) { interceptor(it) }
        }
        return installed
    }

    context(module: Module)
    override fun interceptor(executable: Executable): Hooker {
        val prefs = module.preferences
        val override = overrides[executable.name]
        return Hooker { chain ->
            val key = chain.getArg(0) as String
            if (prefs.isEnabled(preferenceKey) && key == propertyKey) {
                Log.d("${executable.name}($key) reported as $override")
                override
            } else {
                chain.proceed()
            }
        }
    }

    companion object {
        private const val SYSTEM_PROPERTIES_CLASS_NAME = "android.os.SystemProperties"

        /**
         * Hides [propertyKey]: the string getters report [value] as is, while the
         * typed getters parse it.
         */
        fun of(propertyKey: String, value: String, preferenceKey: String) = SystemPropertyHook(
            propertyKey = propertyKey,
            preferenceKey = preferenceKey,
            overrides = mapOf(
                "get" to value,
                "getprop" to value,
                "getBoolean" to value.toBoolean(),
                "getInt" to (value.toIntOrNull() ?: 0),
                "getLong" to (value.toLongOrNull() ?: 0L),
            ),
        )
    }
}
