package top.ltfan.notdeveloper.xposed

import android.annotation.SuppressLint
import android.content.Context
import android.os.IBinder
import android.provider.Settings
import top.ltfan.notdeveloper.service.INotDevService
import top.ltfan.notdeveloper.service.INotificationCallback
import top.ltfan.notdeveloper.detection.DetectionMethod

//abstract class NotDevService : Service() {
//    override fun onBind(intent: Intent): IBinder = object : INotDevService.Stub() {
//        override fun notifySettingChange(method: DetectionMethod, callback: INotificationCallback) {
//            notify(method)
//            callback.callback()
//        }
//    }
//
//    protected abstract fun notify(method: DetectionMethod)
//}
//
//inline fun NotDevService(crossinline notify: (DetectionMethod) -> Unit) = object : NotDevService() {
//    override fun notify(method: DetectionMethod) = notify(method)
//}

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

inline fun NotDevService(crossinline notify: (name: String, type: Int) -> Unit) =
    object : NotDevService() {
        override fun notify(name: String, type: Int) = notify(name, type)
    }

val Context.notDevService
    get() = runCatching {
//        @SuppressLint("PrivateApi")
//        val systemManagerClass = Class.forName("android.os.ServiceManager")
//        val getServiceMethod = systemManagerClass.getMethod("getService", String::class.java)
//        val binder = getServiceMethod.invoke(null, NotDevService::class.java.name) as IBinder
        val binder = contentResolver.call(
            NotDevServiceProvider.uri, CallMethodGet, null, null
        )?.getBinder(BundleExtraService)
            ?: error("Failed to get NotDevService binder")
        INotDevService.Stub.asInterface(binder)
    }.getOrElse {
        Log.Android.e("Failed to get NotDevService: ${it.message}", it)
        null
    }

inline fun INotDevService.notifySettingChange(
    method: DetectionMethod.SettingsMethod,
    crossinline callback: () -> Unit
) {
    notifySettingChange(
        method.settingKey,
        when (method.settingsClass) {
            Settings.Global::class.java -> 0
            Settings.System::class.java -> 1
            Settings.Secure::class.java -> 2
            else -> {
                Log.Android.e("Unknown settings class: ${method.settingsClass}")
                return
            }
        },
        object : INotificationCallback.Stub() {
            override fun invoke() {
                callback()
            }
        },
    )
}
