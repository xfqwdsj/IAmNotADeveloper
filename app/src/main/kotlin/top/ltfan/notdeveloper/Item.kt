package top.ltfan.notdeveloper

import androidx.annotation.StringRes

enum class Item(val key: String, @StringRes val nameId: Int) {
    DevelopmentSettingsEnabled("development_settings_enabled", R.string.toggle_hide_development_mode),
    AdbEnabled("adb_enabled", R.string.toggle_hide_usb_debugging),
    AdbWifiEnabled("adb_wifi_enabled", R.string.toggle_hide_wireless_debugging);

    companion object {
        val oldApiItems = listOf(DevelopmentSettingsEnabled, AdbEnabled)
        val newApiItems = listOf(AdbWifiEnabled)
        val settingGlobalItems = listOf(DevelopmentSettingsEnabled, AdbEnabled, AdbWifiEnabled)
    }
}
