package top.ltfan.notdeveloper.detection

import android.content.Context
import androidx.annotation.StringRes
import top.ltfan.notdeveloper.util.SystemPropsUtil

sealed class DetectionCategory(
    @param:StringRes val labelResId: Int
) {
    val methods: List<DetectionMethod> = this::class.nestedClasses
        .map { it.objectInstance!! as DetectionMethod }

    data object DevelopmentMode : DetectionCategory(
        labelResId = top.ltfan.notdeveloper.R.string.category_development_mode
    ) {
        data object Development : DetectionMethod.SettingsMethod(
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

        data object DevelopmentLegacy : DetectionMethod.SettingsMethod(
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

        data object Debuggable : DetectionMethod.SystemPropertiesMethod(
            name = "ro_debuggable",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_development_mode_debuggable,
            propertyKey = "ro.debuggable",
            overrideValue = "0",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.equalsValue("ro.debuggable", "1")
            }
        }

        data object DebuggableForce : DetectionMethod.SystemPropertiesMethod(
            name = "ro_force_debuggable",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_development_mode_debuggable_force,
            propertyKey = "ro.force.debuggable",
            overrideValue = "0",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.equalsValue("ro.force.debuggable", "1")
            }
        }
    }

    data object UsbDebugging : DetectionCategory(
        labelResId = top.ltfan.notdeveloper.R.string.category_usb_debugging
    ) {
        data object Adb : DetectionMethod.SettingsMethod(
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

        data object AdbLegacy : DetectionMethod.SettingsMethod(
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

        data object AdbSecure : DetectionMethod.SystemPropertiesMethod(
            name = "ro_adb_secure",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_secure,
            propertyKey = "ro.adb.secure",
            overrideValue = "1",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.equalsValue("ro.adb.secure", "0")
            }
        }

        data object ServiceAdbRoot : DetectionMethod.SystemPropertiesMethod(
            name = "service_adb_root",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_root,
            propertyKey = "service.adb.root",
            overrideValue = "0",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.equalsValue("service.adb.root", "1")
            }
        }

        data object AdbSystemPropsUsbState : DetectionMethod.SystemPropertiesMethod(
            name = "adb_system_props_usb_state",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_usb_state,
            propertyKey = "sys.usb.state",
            overrideValue = "mtp",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.containsValue("sys.usb.state", "adb")
            }
        }

        data object AdbSystemPropsUsbConfig : DetectionMethod.SystemPropertiesMethod(
            name = "adb_system_props_usb_config",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_usb_config,
            propertyKey = "sys.usb.config",
            overrideValue = "mtp",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.containsValue("sys.usb.config", "adb")
            }
        }

        data object AdbSystemPropsRebootFunc : DetectionMethod.SystemPropertiesMethod(
            name = "adb_system_props_reboot_func",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_reboot_func,
            propertyKey = "persist.sys.usb.reboot.func",
            overrideValue = "mtp",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.containsValue("persist.sys.usb.reboot.func", "adb")
            }
        }

        data object AdbSystemPropsSvcAdbd : DetectionMethod.SystemPropertiesMethod(
            name = "adb_system_props_svc_adbd",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_svc_adbd,
            propertyKey = "init.svc.adbd",
            overrideValue = "stopped",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.equalsValue("init.svc.adbd", "running")
            }
        }

        data object AdbSystemPropsFfsReady : DetectionMethod.SystemPropertiesMethod(
            name = "adb_system_props_ffs_ready",
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

        data object AdbTcpPort : DetectionMethod.SystemPropertiesMethod(
            name = "persist_adb_tcp_port",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_tcp_port,
            propertyKey = "persist.adb.tcp.port",
            overrideValue = "-1",
        ) {
            override fun test(context: Context): Boolean {
                return !SystemPropsUtil.equalsValue("persist.adb.tcp.port", "-1")
            }
        }

        data object AdbTcpPortService : DetectionMethod.SystemPropertiesMethod(
            name = "service_adb_tcp_port",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_tcp_port_service,
            propertyKey = "service.adb.tcp.port",
            overrideValue = "-1",
        ) {
            override fun test(context: Context): Boolean {
                return !SystemPropsUtil.equalsValue("service.adb.tcp.port", "-1")
            }
        }

        data object UsbMassStorage : DetectionMethod.SettingsMethod(
            name = "usb_mass_storage_enabled",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_usb_mass_storage,
            settingsClass = android.provider.Settings.Global::class.java,
            settingKey = "usb_mass_storage_enabled",
        ) {
            override fun test(context: Context): Boolean {
                return android.provider.Settings.Global.getInt(
                    context.contentResolver,
                    "usb_mass_storage_enabled",
                    0
                ) == 1
            }
        }
    }

    data object WirelessDebugging : DetectionCategory(
        labelResId = top.ltfan.notdeveloper.R.string.category_wireless_debugging
    ) {
        data object AdbWifiEnabled : DetectionMethod.SettingsMethod(
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

    data object Bootloader : DetectionCategory(
        labelResId = top.ltfan.notdeveloper.R.string.category_bootloader
    ) {
        data object VerifiedBootState : DetectionMethod.SystemPropertiesMethod(
            name = "ro_boot_verifiedbootstate",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_bootloader_verified_state,
            propertyKey = "ro.boot.verifiedbootstate",
            overrideValue = "green",
        ) {
            override fun test(context: Context): Boolean {
                return !SystemPropsUtil.equalsValue("ro.boot.verifiedbootstate", "green")
            }
        }

        data object FlashLocked : DetectionMethod.SystemPropertiesMethod(
            name = "ro_boot_flash_locked",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_bootloader_flash_locked,
            propertyKey = "ro.boot.flash.locked",
            overrideValue = "1",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.equalsValue("ro.boot.flash.locked", "0")
            }
        }

        data object VerityMode : DetectionMethod.SystemPropertiesMethod(
            name = "ro_boot_veritymode",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_bootloader_verity_mode,
            propertyKey = "ro.boot.veritymode",
            overrideValue = "enforcing",
        ) {
            override fun test(context: Context): Boolean {
                return !SystemPropsUtil.equalsValue("ro.boot.veritymode", "enforcing")
            }
        }
    }

    data object OemUnlock : DetectionCategory(
        labelResId = top.ltfan.notdeveloper.R.string.category_oem_unlock
    ) {
        data object OemUnlockAllowed : DetectionMethod.SystemPropertiesMethod(
            name = "sys_oem_unlock_allowed",
            labelResId = top.ltfan.notdeveloper.R.string.toggle_hide_oem_unlock,
            propertyKey = "sys.oem_unlock_allowed",
            overrideValue = "0",
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.equalsValue("sys.oem_unlock_allowed", "1")
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
