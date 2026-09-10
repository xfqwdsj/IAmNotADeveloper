package top.ltfan.notdeveloper

import android.app.Application
import io.github.libxposed.service.XposedServiceHelper

/** Module app, which is where the framework delivers its service to. */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // The framework may deliver the service before or after this call; XposedServiceHelper
        // replays services it received earlier, so registering once here is enough.
        XposedServiceHelper.registerListener(ModuleService)
    }
}
