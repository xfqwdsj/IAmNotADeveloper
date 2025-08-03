package top.ltfan.notdeveloper.detection

import android.content.Context
import androidx.annotation.StringRes
import top.ltfan.notdeveloper.util.SystemPropsUtil

sealed class DetectionCategory(
    @param:StringRes val labelResId: Int
) {
    val methods: List<DetectionMethod> = this::class.nestedClasses
        .map { it.objectInstance!! as DetectionMethod }

    object DevelopmentMode : DetectionCategory(
        labelResId = top.ltfan.notdeveloper.R.string.category_development_mode
    ) {
        object Development : DetectionMethod.SettingsMethod(
            name = "development_settings_enabled",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_development_mode,
            settingsClass = android.provider.Settings.Global::class.java,
            settingKey = "development_settings_enabled",
        ) {
            override fun test(context: Context): Boolean {
                return android.provider.Settings.Global.getInt(
                    context.contentResolver,
                    "development_settings_enabled",
                    0
                ) == 1
            }
        }

        object DevelopmentLegacy : DetectionMethod.SettingsMethod(
            name = "development_settings_enabled_legacy",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_development_mode_legacy,
            settingsClass = android.provider.Settings.Secure::class.java,
            settingKey = "development_settings_enabled",
        ) {
            override fun test(context: Context): Boolean {
                return android.provider.Settings.Secure.getInt(
                    context.contentResolver,
                    "development_settings_enabled",
                    0
                ) == 1
            }
        }
    }

    object UsbDebugging : DetectionCategory(
        labelResId = top.ltfan.notdeveloper.R.string.category_usb_debugging
    ) {
        object Adb : DetectionMethod.SettingsMethod(
            name = "adb_enabled",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_usb_debugging,
            settingsClass = android.provider.Settings.Global::class.java,
            settingKey = "adb_enabled",
        ) {
            override fun test(context: Context): Boolean {
                return android.provider.Settings.Global.getInt(
                    context.contentResolver,
                    "adb_enabled",
                    0
                ) == 1
            }
        }

        object AdbLegacy : DetectionMethod.SettingsMethod(
            name = "adb_enabled_legacy",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_usb_debugging_legacy,
            settingsClass = android.provider.Settings.Secure::class.java,
            settingKey = "adb_enabled",
        ) {
            override fun test(context: Context): Boolean {
                return android.provider.Settings.Secure.getInt(
                    context.contentResolver,
                    "adb_enabled",
                    0
                ) == 1
            }
        }

        object AdbSystemPropsUsbState : DetectionMethod.SystemPropertiesMethod(
            preferenceKey = "adb_system_props_usb_state",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_usb_state,
            propertyKey = "sys.usb.state",
            overrideValue = "mtp",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.containsValue("sys.usb.state", "adb")
            }
        }

        object AdbSystemPropsUsbConfig : DetectionMethod.SystemPropertiesMethod(
            preferenceKey = "adb_system_props_usb_config",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_usb_config,
            propertyKey = "sys.usb.config",
            overrideValue = "mtp",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.containsValue("sys.usb.config", "adb")
            }
        }

        object AdbSystemPropsRebootFunc : DetectionMethod.SystemPropertiesMethod(
            preferenceKey = "adb_system_props_reboot_func",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_reboot_func,
            propertyKey = "persist.sys.usb.reboot.func",
            overrideValue = "mtp",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.containsValue("persist.sys.usb.reboot.func", "adb")
            }
        }

        object AdbSystemPropsSvcAdbd : DetectionMethod.SystemPropertiesMethod(
            preferenceKey = "adb_system_props_svc_adbd",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_svc_adbd,
            propertyKey = "init.svc.adbd",
            overrideValue = "stopped",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.equalsValue("init.svc.adbd", "running")
            }
        }

        object AdbSystemPropsFfsReady : DetectionMethod.SystemPropertiesMethod(
            preferenceKey = "adb_system_props_ffs_ready",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_ffs_ready,
            propertyKey = "sys.usb.ffs.ready",
            overrideValue = "0",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.equalsValue("sys.usb.ffs.ready", "1")
            }

            override fun getOverrideValue(methodName: String): Any? = when (methodName) {
                "get", "getprop" -> "0"
                "getBoolean" -> false
                "getInt" -> 0
                "getLong" -> 0L
                else -> "0"
            }
        }
    }

    object WirelessDebugging : DetectionCategory(
        labelResId = top.ltfan.notdeveloper.R.string.category_wireless_debugging
    ) {
        object AdbWifiEnabled : DetectionMethod.SettingsMethod(
            name = "adb_wifi_enabled",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_wireless_debugging,
            settingsClass = android.provider.Settings.Global::class.java,
            settingKey = "adb_wifi_enabled",
        ) {
            override fun test(context: Context): Boolean {
                return android.provider.Settings.Global.getInt(
                    context.contentResolver,
                    "adb_wifi_enabled",
                    0
                ) == 1
            }
        }
    }

    companion object {
        val values: List<DetectionCategory> = DetectionCategory::class.sealedSubclasses
            .map { it.objectInstance!! }

        val allMethods: List<DetectionMethod>
            get() = values.flatMap { it.methods }
    }
}
