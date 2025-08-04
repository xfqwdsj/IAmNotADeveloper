package top.ltfan.notdeveloper.detection

import android.content.Context
import androidx.annotation.StringRes

sealed class DetectionMethod(
    val name: String,
    @param:StringRes val labelResId: Int,
) {
    abstract fun test(context: Context): Boolean

    abstract class SettingsMethod(
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

    abstract class SystemPropertiesMethod(
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
