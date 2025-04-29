package xyz.xfqlittlefan.notdeveloper.xposed

import de.robv.android.xposed.XposedBridge

object Log {
    const val TAG = "NotDeveloper"

    fun d(message: String) {
        log("DEBUG", message)
    }

    fun i(message: String) {
        log("INFO", message)
    }

    fun w(message: String) {
        log("WARN", message)
    }

    private fun log(level: String, message: String) {
        XposedBridge.log("[$level] $TAG: $message")
    }
}