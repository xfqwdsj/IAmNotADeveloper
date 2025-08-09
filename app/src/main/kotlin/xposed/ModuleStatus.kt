package top.ltfan.notdeveloper.xposed

import android.annotation.SuppressLint
import android.content.Context
import top.ltfan.notdeveloper.log.Log

@Suppress("MayBeConstant", "RedundantSuppression")
val statusIsModuleActivated get() = StatusProxy.get()

object StatusProxy {
    fun get() = false
}

val Context.statusIsPreferencesReady: Boolean
    @SuppressLint("WorldReadableFiles") get() {
        return try {
            @Suppress("DEPRECATION") getSharedPreferences(
                "testPreferences",
                Context.MODE_WORLD_READABLE
            )
            true
        } catch (t: Throwable) {
            Log.Android.e("failed to confirm the state of SharedPreferences", t)
            false
        }
    }
