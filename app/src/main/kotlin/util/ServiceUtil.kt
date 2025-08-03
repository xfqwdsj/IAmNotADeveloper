package top.ltfan.notdeveloper.util

import android.os.IInterface
import android.os.RemoteCallbackList

fun <T : IInterface> RemoteCallbackList<T>.doBroadcast(action: (T) -> Unit) {
    val count = beginBroadcast()
    try {
        for (i in 0 until count) {
            action(getBroadcastItem(i))
        }
    } finally {
        finishBroadcast()
    }
}
