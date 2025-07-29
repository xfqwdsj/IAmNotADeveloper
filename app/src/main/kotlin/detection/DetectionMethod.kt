package top.ltfan.notdeveloper.detection

import android.content.Context
import androidx.annotation.StringRes

sealed class DetectionMethod(
    val preferenceKey: String,
    @param:StringRes val nameId: Int
) {
    abstract fun test(context: Context): Boolean

    abstract class SettingsMethod(
        preferenceKey: String,
        nameId: Int,
        val settingsClass: Class<*>,
        val settingKey: String,
    ) : DetectionMethod(preferenceKey, nameId)

    abstract class SystemPropertiesMethod(
        preferenceKey: String,
        nameId: Int,
        val propertyKey: String,
        val overrideValue: String,
    ) : DetectionMethod(preferenceKey, nameId) {
        open fun getOverrideValue(methodName: String): Any? = when (methodName) {
            "get", "getprop" -> overrideValue
            "getBoolean" -> overrideValue.toBoolean()
            "getInt" -> overrideValue.toIntOrNull() ?: 0
            "getLong" -> overrideValue.toLongOrNull() ?: 0L
            else -> overrideValue
        }
    }
}
