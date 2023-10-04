package xyz.xfqlittlefan.notdeveloper.xposed

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.Keep

val isModuleActive
    @Keep get() = false

@SuppressLint("WorldReadableFiles")
fun Context.isPreferencesReady(): Boolean {
    return try {
        getSharedPreferences("testPreferences", Context.MODE_WORLD_READABLE)
        true
    } catch (e: Throwable) {
        false
    }
}
