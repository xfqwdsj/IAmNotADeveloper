package top.ltfan.notdeveloper.xposed

import android.content.Context
import android.provider.Settings
import top.ltfan.dslutilities.LockableValueDsl
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.provider.getInterfaceOrNull
import top.ltfan.notdeveloper.service.INotDevService
import top.ltfan.notdeveloper.service.INotificationCallback
import top.ltfan.notdeveloper.service.data.IPackageSettingsDao
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

const val CallMethodGet = "GET"
const val CallMethodNotify = "NOTIFY"
const val BundleExtraService = "service"
const val BundleExtraType = "type"

abstract class NotDevService : INotDevService.Stub() {
    override fun notifySettingChange(name: String, type: Int, callback: INotificationCallback) {
        notify(name, type)
        callback()
    }

    protected abstract fun notify(name: String, type: Int)

    @get:JvmName("getConnectionsKotlin")
    private val connections = DaoConnectionsMap()

    override fun getConnections() = connections
}

class DaoConnectionsMap(
    private val delegate: ConcurrentHashMap<String, IPackageSettingsDao> = ConcurrentHashMap(),
) : ConcurrentMap<String, IPackageSettingsDao> by delegate {
//    override fun get(key: String?): IPackageSettingsDao? {
//        val result = delegate[key]
//        if (result == null) return result
//
//
//    }
}

@NotDevServiceBuilder.Dsl
class NotDevServiceBuilder : LockableValueDsl() {
    var notify by required<(name: String, type: Int) -> Unit>()

    fun notify(block: (name: String, type: Int) -> Unit) {
        notify = block
    }

    fun build(): NotDevService {
        lock()
        return object : NotDevService() {
            override fun notify(name: String, type: Int) {
                notify.invoke(name, type)
            }
        }
    }

    companion object {
        inline fun build(block: NotDevServiceBuilder.() -> Unit): NotDevService =
            NotDevServiceBuilder().apply(block).build()
    }

    @DslMarker
    annotation class Dsl
}

inline fun NotDevService(block: NotDevServiceBuilder.() -> Unit) =
    NotDevServiceBuilder.build(block)

val Context.notDevService
    get() = runCatching {
        contentResolver.getInterfaceOrNull(NotDevServiceProvider) {
            NotDevClient(INotDevService.Stub.asInterface(it))
        } ?: error("Failed to get NotDevService binder")
    }.getOrElse {
        Log.Android.e("Failed to get NotDevService: ${it.message}", it)
        null
    }

class NotDevClient(service: INotDevService) : INotDevService by service

inline fun INotDevService.notifySettingChange(
    method: DetectionMethod.SettingsMethod, crossinline callback: () -> Unit
) {
    notifySettingChange(
        method.settingKey,
        when (method.settingsClass) {
            Settings.Global::class.java -> 0
            Settings.System::class.java -> 1
            Settings.Secure::class.java -> 2
            else -> error("Unknown settings class: ${method.settingsClass}")
        },
        object : INotificationCallback.Stub() {
            override fun invoke() {
                callback()
            }
        },
    )
}
