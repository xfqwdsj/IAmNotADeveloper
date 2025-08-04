package top.ltfan.notdeveloper.xposed

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import androidx.room.Room
import top.ltfan.dslutilities.LockableValueDsl
import top.ltfan.notdeveloper.data.PackageSettingsDao
import top.ltfan.notdeveloper.data.PackageSettingsDatabase
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.service.INotDevService
import top.ltfan.notdeveloper.service.INotificationCallback
import top.ltfan.notdeveloper.service.data.IPackageSettingsDao
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.full.staticFunctions

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
        val binder = contentResolver.call(
            NotDevServiceProvider.uri, CallMethodGet, null, null
        )?.getBinder(BundleExtraService) ?: error("Failed to get NotDevService binder")
        NotDevServiceClient(this, INotDevService.Stub.asInterface(binder))
    }.getOrElse {
        Log.Android.e("Failed to get NotDevService: ${it.message}", it)
        null
    }

class NotDevServiceClient(
    context: Context,
    service: INotDevService,
) : INotDevService by service {
    val dao: PackageSettingsDao

    init {
        val application = context.applicationContext
        val myUserId = UserHandle::class.staticFunctions
            .firstOrNull { it.name == "myUserId" } ?: error("Failed to get myUserId method")
        val userId = (myUserId.call() as Int).toString()
        val database = Room.databaseBuilder(
            application,
            PackageSettingsDatabase::class.java,
            PackageSettingsDatabase.DATABASE_NAME,
        ).build()
        val dao = database.dao()
        val daoService = PackageSettingsDaoService(dao)
        service.connections[userId] = daoService
        this.dao = dao
    }
}

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
