package top.ltfan.notdeveloper.xposed.hook

import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import top.ltfan.notdeveloper.xposed.Module
import java.lang.reflect.Executable

/**
 * Hides a system property by overriding the value every getter that reads
 * it reports.
 *
 * [overrides] maps the name of a `SystemProperties` method to the value it
 * has to report instead; its keys also decide which methods are hooked.
 * The actual interception is delegated to [PropertyBackends.backend],
 * so a native backend can take over without changing this hook.
 */
class SystemPropertyHook(
    private val propertyKey: String,
    private val preferenceKey: String,
    private val overrides: Map<String, Any>,
) : Hook {
    context(module: Module)
    override fun install(param: PackageReadyParam): Int {
        val prefs = module.preferences
        return PropertyBackends.backend.install(param, propertyKey, overrides) {
            prefs.isEnabled(param.packageName, preferenceKey)
        }
    }

    context(module: Module)
    override fun interceptor(executable: Executable, packageName: String): Hooker {
        val prefs = module.preferences
        return PropertyBackends.backend.interceptor(executable, propertyKey, overrides) {
            prefs.isEnabled(packageName, preferenceKey)
        }
    }

    companion object {
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
