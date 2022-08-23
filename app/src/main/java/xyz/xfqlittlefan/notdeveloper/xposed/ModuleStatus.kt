package xyz.xfqlittlefan.notdeveloper.xposed

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.Keep

val isModuleActive
    @Keep get() = false

@SuppressLint("WorldReadableFiles")
fun Context.isPreferencesReady(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        return true
    }
    return try {
        getSharedPreferences("testPreferences", Context.MODE_WORLD_READABLE)
        true
    } catch (e: Throwable) {
        false
    }
}