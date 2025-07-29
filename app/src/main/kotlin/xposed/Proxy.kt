package top.ltfan.notdeveloper.xposed

object SettingsType {
    const val GLOBAL = 0
    const val SYSTEM = 1
    const val SECURE = 2

    fun fromClass(clazz: Class<*>): Int {
        return when (clazz) {
            android.provider.Settings.Global::class.java -> GLOBAL
            android.provider.Settings.System::class.java -> SYSTEM
            android.provider.Settings.Secure::class.java -> SECURE
            else -> 0
        }
    }
}

data class SettingsChange(
    val type: Int,
    val name: String,
)
