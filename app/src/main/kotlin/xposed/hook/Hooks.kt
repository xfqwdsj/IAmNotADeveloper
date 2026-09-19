package top.ltfan.notdeveloper.xposed.hook

import android.provider.Settings
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod

/**
 * The hook that hides [this] detection method from other apps.
 *
 * [DetectionMethod] is sealed, so adding a detection method without
 * deciding how to hide it is a compile error.
 */
val DetectionMethod.hook: Hook
    get() = when (this) {
        is DetectionCategory.DevelopmentMode.Development ->
            SettingsHook(Settings.Global::class, "development_settings_enabled", preferenceKey)

        is DetectionCategory.DevelopmentMode.DevelopmentLegacy ->
            SettingsHook(Settings.Secure::class, "development_settings_enabled", preferenceKey)

        is DetectionCategory.DevelopmentMode.Debuggable ->
            SystemPropertyHook.of("ro.debuggable", "0", preferenceKey)

        is DetectionCategory.DevelopmentMode.DebuggableForce ->
            SystemPropertyHook.of("ro.force.debuggable", "0", preferenceKey)

        is DetectionCategory.UsbDebugging.Adb ->
            SettingsHook(Settings.Global::class, "adb_enabled", preferenceKey)

        is DetectionCategory.UsbDebugging.AdbLegacy ->
            SettingsHook(Settings.Secure::class, "adb_enabled", preferenceKey)

        is DetectionCategory.UsbDebugging.AdbSecure ->
            SystemPropertyHook.of("ro.adb.secure", "1", preferenceKey)

        is DetectionCategory.UsbDebugging.ServiceAdbRoot ->
            SystemPropertyHook.of("service.adb.root", "0", preferenceKey)

        is DetectionCategory.UsbDebugging.AdbSystemPropsUsbState ->
            SystemPropertyHook.of("sys.usb.state", "mtp", preferenceKey)

        is DetectionCategory.UsbDebugging.AdbSystemPropsUsbConfig ->
            SystemPropertyHook.of("sys.usb.config", "mtp", preferenceKey)

        is DetectionCategory.UsbDebugging.AdbSystemPropsRebootFunc ->
            SystemPropertyHook.of("persist.sys.usb.reboot.func", "mtp", preferenceKey)

        is DetectionCategory.UsbDebugging.AdbSystemPropsSvcAdbd ->
            SystemPropertyHook.of("init.svc.adbd", "stopped", preferenceKey)

        is DetectionCategory.UsbDebugging.AdbSystemPropsFfsReady ->
            SystemPropertyHook.of("sys.usb.ffs.ready", "0", preferenceKey)

        is DetectionCategory.UsbDebugging.AdbTcpPort ->
            SystemPropertyHook.of("persist.adb.tcp.port", "-1", preferenceKey)

        is DetectionCategory.UsbDebugging.AdbTcpPortService ->
            SystemPropertyHook.of("service.adb.tcp.port", "-1", preferenceKey)

        is DetectionCategory.UsbDebugging.UsbMassStorage ->
            SettingsHook(Settings.Global::class, "usb_mass_storage_enabled", preferenceKey)

        is DetectionCategory.WirelessDebugging.AdbWifiEnabled ->
            SettingsHook(Settings.Global::class, "adb_wifi_enabled", preferenceKey)

        is DetectionCategory.Bootloader.VerifiedBootState ->
            SystemPropertyHook.of("ro.boot.verifiedbootstate", "green", preferenceKey)

        is DetectionCategory.Bootloader.FlashLocked ->
            SystemPropertyHook.of("ro.boot.flash.locked", "1", preferenceKey)

        is DetectionCategory.Bootloader.VerityMode ->
            SystemPropertyHook.of("ro.boot.veritymode", "enforcing", preferenceKey)

        is DetectionCategory.OemUnlock.OemUnlockAllowed ->
            SystemPropertyHook.of("sys.oem_unlock_allowed", "0", preferenceKey)
    }
