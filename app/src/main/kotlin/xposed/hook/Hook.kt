package top.ltfan.notdeveloper.xposed.hook

import android.content.SharedPreferences
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import top.ltfan.notdeveloper.xposed.Log
import top.ltfan.notdeveloper.xposed.Module
import java.lang.reflect.Executable
import java.lang.reflect.Method

/**
 * A single, self-contained hook of this module.
 *
 * libxposed hooks one [Executable] at a time, so a hook resolves the
 * executables it targets and creates one interceptor for each of them.
 */
sealed interface Hook {
    /**
     * Installs this hook for the package described by [param].
     *
     * @return the number of executables that were intercepted.
     */
    context(module: Module)
    fun install(param: PackageReadyParam): Int

    /** Creates the interceptor used for a single target [executable]. */
    context(module: Module)
    fun interceptor(executable: Executable): Hooker
}

/**
 * Hooks every method named [methodName] declared by [type].
 *
 * libxposed hooks one [Executable] at a time, so the matching executables
 * are enumerated and hooked individually.
 *
 * @return the number of executables that were intercepted, so the caller
 *    can tell a missing target apart from an installed hook.
 */
context(module: Module)
internal fun installAllMethods(
    type: Class<*>,
    methodName: String,
    interceptor: (Method) -> Hooker,
): Int {
    var installed = 0
    type.declaredMethods.filter { it.name == methodName }.forEach { method ->
        try {
            module.hook(method).intercept(interceptor(method))
            installed++
            Log.d("hooked ${type.name}#${method.name}")
        } catch (e: Exception) {
            Log.w("failed to hook ${type.name}#${method.name}", e)
        }
    }
    return installed
}

/**
 * Whether the hook guarded by [preferenceKey] is enabled.
 *
 * The framework keeps remote preferences in sync with the module app,
 * so reading them returns the setting the module app stores. A `null`
 * receiver means the framework delivers no remote preferences; every hook
 * then stays enabled, which the UI reports as "Preferences not working".
 */
internal fun SharedPreferences?.isEnabled(preferenceKey: String): Boolean =
    this?.getBoolean(preferenceKey, true) ?: true
