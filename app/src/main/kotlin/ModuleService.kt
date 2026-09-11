package top.ltfan.notdeveloper

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import top.ltfan.notdeveloper.xposed.Log
import top.ltfan.notdeveloper.xposed.RemotePreferencesGroup

/**
 * Connection between the module app and the Xposed framework.
 *
 * [XposedServiceHelper] delivers an [XposedService] once the framework
 * knows this module, and [XposedService.getRemotePreferences] returns the
 * preferences that the hooked packages read through the framework. Both
 * properties are Compose state, so the UI follows the service being bound
 * and unbound.
 */
object ModuleService : XposedServiceHelper.OnServiceListener {
    /**
     * The framework of the connected Xposed manager, or `null` while no
     * framework is connected.
     */
    var service: XposedService? by mutableStateOf(null)
        private set

    /**
     * Remote preferences shared with the hooked packages, or `null` while the
     * framework delivers none.
     */
    var preferences: SharedPreferences? by mutableStateOf(null)
        private set

    /** Whether the framework is connected, i.e. the module is activated. */
    val isActivated get() = service != null

    override fun onServiceBind(service: XposedService) {
        this.service = service
        preferences = try {
            service.getRemotePreferences(RemotePreferencesGroup)
        } catch (e: Exception) {
            // Every hook uses its default while the framework delivers no remote preferences.
            Log.Android.e("remote preferences are unavailable", e)
            null
        }
    }

    override fun onServiceDied(service: XposedService) {
        if (this.service === service) {
            this.service = null
            preferences = null
        }
    }
}
