package top.ltfan.notdeveloper.application

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import top.ltfan.material.m3.datastore.StoreHost
import top.ltfan.notdeveloper.App
import top.ltfan.notdeveloper.database.PackageSettingsDatabase
import top.ltfan.notdeveloper.datastore.AppFile
import top.ltfan.notdeveloper.datastore.AppListSettingsStore
import top.ltfan.notdeveloper.datastore.GlobalPreferencesStore
import top.ltfan.notdeveloper.settings.UiSettingsModelStore

class NotDevApplication : App() {
    val database by lazy { PackageSettingsDatabase.get() }

    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Process-wide store host. One host for the whole process keeps the loaded
     * values in memory, so recreating the UI reads the warm values directly.
     */
    val storeHost by lazy {
        StoreHost(storeScope, AppFile(this)).also { host ->
            // Load every persisted model eagerly, before the first composition.
            host.mutable(AppListSettingsStore)
            host.mutable(UiSettingsModelStore)
            host.mutable(GlobalPreferencesStore)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Warm the stores up as early as possible.
        storeHost
    }
}
