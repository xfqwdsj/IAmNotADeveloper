package top.ltfan.notdeveloper.detection

import android.content.Context
import androidx.annotation.StringRes
import top.ltfan.notdeveloper.util.SystemPropsUtil

sealed class DetectionCategory(
    @param:StringRes val nameId: Int
) {
    val methods: List<DetectionMethod> = this::class.nestedClasses
        .map { it.objectInstance!! as DetectionMethod }

    object DevelopmentMode : DetectionCategory(
        nameId = top.ltfan.notdeveloper.R.string.category_development_mode
    ) {
        object Development : DetectionMethod(
            preferenceKey = "development_settings_enabled",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_development_mode
        ) {
            override fun test(context: Context): Boolean {
                return android.provider.Settings.Global.getInt(
                    context.contentResolver,
                    "development_settings_enabled",
                    0
                ) == 1
            }
        }

        object DevelopmentLegacy : DetectionMethod(
            preferenceKey = "development_settings_enabled_legacy",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_development_mode_legacy
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
        nameId = top.ltfan.notdeveloper.R.string.category_usb_debugging
    ) {
        object Adb : DetectionMethod(
            preferenceKey = "adb_enabled",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_usb_debugging
        ) {
            override fun test(context: Context): Boolean {
                return android.provider.Settings.Global.getInt(
                    context.contentResolver,
                    "adb_enabled",
                    0
                ) == 1
            }
        }

        object AdbLegacy : DetectionMethod(
            preferenceKey = "adb_enabled_legacy",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_usb_debugging_legacy
        ) {
            override fun test(context: Context): Boolean {
                return android.provider.Settings.Secure.getInt(
                    context.contentResolver,
                    "adb_enabled",
                    0
                ) == 1
            }
        }

        object AdbSystemPropsUsbState : DetectionMethod(
            preferenceKey = "adb_system_props_usb_state",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_usb_state
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.containsValue("sys.usb.state", "adb")
            }
        }

        object AdbSystemPropsUsbConfig : DetectionMethod(
            preferenceKey = "adb_system_props_usb_config",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_usb_config
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.containsValue("sys.usb.config", "adb")
            }
        }

        object AdbSystemPropsRebootFunc : DetectionMethod(
            preferenceKey = "adb_system_props_reboot_func",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_reboot_func
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.containsValue("persist.sys.usb.reboot.func", "adb")
            }
        }

        object AdbSystemPropsSvcAdbd : DetectionMethod(
            preferenceKey = "adb_system_props_svc_adbd",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_svc_adbd
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.equalsValue("init.svc.adbd", "running")
            }
        }

        object AdbSystemPropsFfsReady : DetectionMethod(
            preferenceKey = "adb_system_props_ffs_ready",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_adb_ffs_ready
        ) {
            override fun test(context: Context): Boolean {
                return SystemPropsUtil.equalsValue("sys.usb.ffs.ready", "1")
            }
        }
    }

    object WirelessDebugging : DetectionCategory(
        nameId = top.ltfan.notdeveloper.R.string.category_wireless_debugging
    ) {
        object AdbWifiEnabled : DetectionMethod(
            preferenceKey = "adb_wifi_enabled",
            nameId = top.ltfan.notdeveloper.R.string.toggle_hide_wireless_debugging
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
