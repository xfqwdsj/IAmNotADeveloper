package top.ltfan.notdeveloper.detection

import android.content.Context
import android.os.Parcel
import androidx.annotation.Keep
import androidx.annotation.StringRes

@Keep
sealed class DetectionCategory(
    @param:StringRes val nameId: Int
) {
    val methods: List<DetectionMethod> = this::class.nestedClasses
        .map { it.objectInstance!! as DetectionMethod }

    @Keep
    object DevelopmentMode : DetectionCategory(
        nameId = top.ltfan.notdeveloper.R.string.category_development_mode
    ) {
        @Keep
        object Development : DetectionMethod.SettingsMethod(
            preferenceKey = "development_settings_enabled",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_development_mode,
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

        @Keep
        object DevelopmentLegacy : DetectionMethod.SettingsMethod(
            preferenceKey = "development_settings_enabled_legacy",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_development_mode_legacy,
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

    @Keep
    object UsbDebugging : DetectionCategory(
        nameId = top.ltfan.notdeveloper.R.string.category_usb_debugging
    ) {
        @Keep
        object Adb : DetectionMethod.SettingsMethod(
            preferenceKey = "adb_enabled",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_usb_debugging,
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

        @Keep
        object AdbLegacy : DetectionMethod.SettingsMethod(
            preferenceKey = "adb_enabled_legacy",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_usb_debugging_legacy,
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

        @Keep
        object AdbSystemPropsUsbState : DetectionMethod.SystemPropertiesMethod(
            preferenceKey = "adb_system_props_usb_state",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_usb_state,
            propertyKey = "sys.usb.state",
            overrideValue = "mtp",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtils.containsValue("sys.usb.state", "adb")
            }
        }

        @Keep
        object AdbSystemPropsUsbConfig : DetectionMethod.SystemPropertiesMethod(
            preferenceKey = "adb_system_props_usb_config",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_usb_config,
            propertyKey = "sys.usb.config",
            overrideValue = "mtp",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtils.containsValue("sys.usb.config", "adb")
            }
        }

        @Keep
        object AdbSystemPropsRebootFunc : DetectionMethod.SystemPropertiesMethod(
            preferenceKey = "adb_system_props_reboot_func",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_reboot_func,
            propertyKey = "persist.sys.usb.reboot.func",
            overrideValue = "mtp",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtils.containsValue("persist.sys.usb.reboot.func", "adb")
            }
        }

        @Keep
        object AdbSystemPropsSvcAdbd : DetectionMethod.SystemPropertiesMethod(
            preferenceKey = "adb_system_props_svc_adbd",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_svc_adbd,
            propertyKey = "init.svc.adbd",
            overrideValue = "stopped",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtils.equalsValue("init.svc.adbd", "running")
            }
        }

        @Keep
        object AdbSystemPropsFfsReady : DetectionMethod.SystemPropertiesMethod(
            preferenceKey = "adb_system_props_ffs_ready",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_ffs_ready,
            propertyKey = "sys.usb.ffs.ready",
            overrideValue = "0",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtils.equalsValue("sys.usb.ffs.ready", "1")
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

    @Keep
    object WirelessDebugging : DetectionCategory(
        nameId = top.ltfan.notdeveloper.R.string.category_wireless_debugging
    ) {
        @Keep
        object AdbWifiEnabled : DetectionMethod.SettingsMethod(
            preferenceKey = "adb_wifi_enabled",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_wireless_debugging,
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
