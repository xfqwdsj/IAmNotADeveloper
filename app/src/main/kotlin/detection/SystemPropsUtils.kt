package top.ltfan.notdeveloper.detection

import android.annotation.SuppressLint

object SystemPropsUtils {
    @SuppressLint("PrivateApi")
    inline fun getProperty(
        key: String,
        defaultValue: String = "",
        failed: (Throwable) -> Unit = { it.printStackTrace() },
    ) = try {
        val clazz = Class.forName("android.os.SystemProperties")
        val method = clazz.getMethod("get", String::class.java, String::class.java)
        method.invoke(null, key, defaultValue) as String
    } catch (e: Throwable) {
        failed(e)
        defaultValue
    }

    fun containsValue(key: String, value: String): Boolean {
        return getProperty(key).contains(value)
    }

    fun equalsValue(key: String, value: String): Boolean {
        return getProperty(key) == value
    }
}
