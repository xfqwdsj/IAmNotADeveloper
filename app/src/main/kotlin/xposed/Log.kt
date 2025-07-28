package top.ltfan.notdeveloper.xposed

import de.robv.android.xposed.XposedBridge

object Log {
    const val TAG = "NotDeveloper"

    fun v(message: String, throwable: Throwable? = null) {
        android.util.Log.v(TAG, message, throwable)
    }

    fun d(message: String, throwable: Throwable? = null) {
        android.util.Log.d(TAG, message, throwable)
    }

    fun i(message: String, throwable: Throwable? = null) {
        android.util.Log.i(TAG, message, throwable)
    }

    fun w(message: String, throwable: Throwable? = null) {
        bridgeLog("WARN", message, throwable)
    }

    fun e(message: String, throwable: Throwable? = null) {
        bridgeLog("ERROR", message, throwable)
    }

    private fun bridgeLog(level: String, message: String, throwable: Throwable? = null) {
        XposedBridge.log("[$level] $TAG: $message")
        if (throwable != null) {
            XposedBridge.log(throwable)
        }
    }
}
