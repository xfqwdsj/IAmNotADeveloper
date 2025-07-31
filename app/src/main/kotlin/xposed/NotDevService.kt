package top.ltfan.notdeveloper.xposed

import android.content.Context
import top.ltfan.notdeveloper.INotDevService
import top.ltfan.notdeveloper.INotificationCallback
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

const val CallMethodNotify = "NOTIFY"
const val BundleExtraType = "type"

abstract class NotDevService : INotDevService.Stub() {
    override fun notifySettingChange(method: DetectionMethod, callback: INotificationCallback) {
        notify(method)
        callback()
    }

    protected abstract fun notify(method: DetectionMethod)
}

inline fun NotDevService(crossinline notify: (DetectionMethod) -> Unit) = object : NotDevService() {
    override fun notify(method: DetectionMethod) = notify(method)
}

val Context.notDevService get() = runCatching {
    INotDevService.Stub.asInterface(
        getSystemService(NotDevService::class.java.name) as android.os.IBinder
    )
}.getOrElse {
    Log.Android.e("Failed to get NotDevService: ${it.message}", it)
    null
}

inline fun INotDevService.notifySettingChange(method: DetectionMethod, crossinline callback: () -> Unit) {
    notifySettingChange(method, object : INotificationCallback.Stub() {
        override fun invoke() {
            callback()
        }
    })
}
