package top.ltfan.notdeveloper.xposed

import de.robv.android.xposed.XposedBridge

const val LogTag = "NotDeveloper"

interface Logger {
    fun v(message: String, throwable: Throwable? = null) {
        android.util.Log.v(LogTag, message, throwable)
    }

    fun d(message: String, throwable: Throwable? = null) {
        android.util.Log.d(LogTag, message, throwable)
    }

    fun i(message: String, throwable: Throwable? = null) {
        android.util.Log.i(LogTag, message, throwable)
    }

    fun w(message: String, throwable: Throwable? = null) {
        android.util.Log.w(LogTag, message, throwable)
    }

    fun e(message: String, throwable: Throwable? = null) {
        android.util.Log.e(LogTag, message, throwable)
    }
}

object Log : Logger {
    override fun w(message: String, throwable: Throwable?) {
        bridgeLog("WARN", message, throwable)
    }

    override fun e(message: String, throwable: Throwable?) {
        bridgeLog("ERROR", message, throwable)
    }

    private fun bridgeLog(level: String, message: String, throwable: Throwable? = null) {
        XposedBridge.log("[$level] $LogTag: $message")
        if (throwable != null) {
            XposedBridge.log(throwable)
        }
    }

    object Android : Logger
}
