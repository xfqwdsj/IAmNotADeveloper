package top.ltfan.notdeveloper.xposed

import androidx.annotation.Keep
import top.ltfan.notdeveloper.detection.DetectionMethod

@Keep
object Notification {
    fun notifySettingsChange(type: Int, name: String, callback: () -> Unit) {
        Log.Android.d("Notified change in settings: type=$type, name=$name")
    }
}
