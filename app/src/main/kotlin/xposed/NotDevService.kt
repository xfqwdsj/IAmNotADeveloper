package top.ltfan.notdeveloper.xposed

import android.content.Context
import android.provider.Settings
import top.ltfan.dslutilities.LockableValueDsl
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.service.INotDevService
import top.ltfan.notdeveloper.service.INotificationCallback

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
        INotDevService.Stub.asInterface(binder)
    }.getOrElse {
        Log.Android.e("Failed to get NotDevService: ${it.message}", it)
        null
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
