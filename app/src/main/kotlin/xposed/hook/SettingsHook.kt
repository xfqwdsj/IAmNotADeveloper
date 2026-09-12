package top.ltfan.notdeveloper.xposed.hook

import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import top.ltfan.notdeveloper.xposed.Log
import top.ltfan.notdeveloper.xposed.Module
import java.lang.reflect.Executable
import kotlin.reflect.KClass

/**
 * Hides one settings entry by forcing `Settings.getStringForUser` to
 * report it as `"0"`.
 *
 * [settingsType] is the concrete `Settings` subclass to hook —
 * `Settings.Global` or `Settings.Secure` — because each subclass declares
 * its own user aware lookup.
 */
class SettingsHook(
    private val settingsType: KClass<*>,
    private val settingKey: String,
    private val preferenceKey: String,
) : Hook {
    context(module: Module)
    override fun install(param: PackageReadyParam): Int =
        installAllMethods(settingsType.java, GET_STRING_FOR_USER) {
            interceptor(it, param.packageName)
        }

    context(module: Module)
    override fun interceptor(executable: Executable, packageName: String): Hooker {
        val prefs = module.preferences
        return Hooker { chain ->
            val name = chain.getArg(1) as String
            if (prefs.isEnabled(packageName, preferenceKey) && name == settingKey) {
                Log.d("${executable.name}($name) reported as 0")
                // Report the hidden value without running the original lookup.
                "0"
            } else {
                chain.proceed()
            }
        }
    }

    private companion object {
        const val GET_STRING_FOR_USER = "getStringForUser"
    }
}
