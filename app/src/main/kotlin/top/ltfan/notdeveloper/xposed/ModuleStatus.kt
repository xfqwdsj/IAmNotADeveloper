package top.ltfan.notdeveloper.xposed

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.Keep

val isModuleActive
    @Keep get() = false

@SuppressLint("WorldReadableFiles")
fun Context.isPreferencesReady(): Boolean {
    return try {
        @Suppress("DEPRECATION") getSharedPreferences(
            "testPreferences",
            Context.MODE_WORLD_READABLE
        )
        true
    } catch (t: Throwable) {
        android.util.Log.e(Log.TAG, "failed to confirm SharedPreferences' state.", t)
        false
    }
}
