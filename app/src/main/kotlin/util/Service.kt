package top.ltfan.notdeveloper.util

import android.os.Binder
import android.os.IInterface
import android.os.RemoteCallbackList

inline fun <T : IInterface> RemoteCallbackList<T>.doBroadcast(action: (T) -> Unit) {
    val count = beginBroadcast()
    try {
        for (i in 0 until count) {
            action(getBroadcastItem(i))
        }
    } finally {
        finishBroadcast()
    }
}

inline fun <R> clearBinderCallingIdentity(block: () -> R) = Binder.clearCallingIdentity().let {
    block().also { _ ->
        Binder.restoreCallingIdentity(it)
    }
}
