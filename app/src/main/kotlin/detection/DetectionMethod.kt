package top.ltfan.notdeveloper.detection

import android.content.Context
import androidx.annotation.StringRes

/**
 * How a detection method is hidden. It tells the configuration and scope
 * logic which mechanism a method relies on.
 */
enum class HookKind {
    /** Hides a `Settings` entry through the settings provider. */
    JvmSettings,

    /** Overrides a `SystemProperties` getter in the target process. */
    JvmProperty,

    /**
     * Reads the property's final value through a native interception, which
     * needs a native module entry and is not available yet.
     */
    NativeProperty,
}

sealed class DetectionMethod(
    val name: String,
    @param:StringRes val labelResId: Int,
) {
    /**
     * Key under which the framework shares this detection's enabled state with
     * the hooked packages.
     */
    val preferenceKey: String get() = name

    /** The mechanism used to hide this method. */
    val hookKind: HookKind
        get() = when (this) {
            is SettingsMethod -> HookKind.JvmSettings
            is SystemPropertiesMethod -> HookKind.JvmProperty
        }

    abstract fun test(context: Context): Boolean

    sealed class SettingsMethod(
        name: String,
        @StringRes labelResId: Int,
        val settingsClass: Class<*>,
        val settingKey: String,
    ) : DetectionMethod(name, labelResId) {
        companion object {
            val all by lazy { DetectionCategory.allMethods.filterIsInstance<SettingsMethod>() }
            fun fromSettingKey(key: String) = all.filter { it.settingKey == key }
        }
    }

    sealed class SystemPropertiesMethod(
        name: String,
        @StringRes labelResId: Int,
        val propertyKey: String,
        val overrideValue: String,
    ) : DetectionMethod(name, labelResId) {
        open fun getOverrideValue(methodName: String): Any? = when (methodName) {
            "get", "getprop" -> overrideValue
            "getBoolean" -> overrideValue.toBoolean()
            "getInt" -> overrideValue.toIntOrNull() ?: 0
            "getLong" -> overrideValue.toLongOrNull() ?: 0L
            else -> overrideValue
        }

        companion object {
            val all by lazy { DetectionCategory.allMethods.filterIsInstance<SystemPropertiesMethod>() }
            fun fromPropertyKey(key: String) = all.filter { it.propertyKey == key }
        }
    }
}
