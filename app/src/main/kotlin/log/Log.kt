package top.ltfan.notdeveloper.log

import android.util.Log
import de.robv.android.xposed.XposedBridge
import top.ltfan.notdeveloper.BuildConfig

const val LogTag = "NotDeveloper"

interface Logger {
    fun v(message: String, throwable: Throwable? = null) {
        Log.v(LogTag, message, throwable)
    }

    fun d(message: String, throwable: Throwable? = null) {
        Log.d(LogTag, message, throwable)
    }

    fun i(message: String, throwable: Throwable? = null) {
        Log.i(LogTag, message, throwable)
    }

    fun w(message: String, throwable: Throwable? = null) {
        Log.w(LogTag, message, throwable)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(LogTag, message, throwable)
    }

    val debug get() = DebugLogger(this)
}

interface XposedLogger : Logger {
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
}

object Log : XposedLogger {
    object Android : Logger
}

class DebugLogger(private val delegate: Logger) : Logger by delegate {
    override fun v(message: String, throwable: Throwable?) {
        if (BuildConfig.DEBUG) {
            delegate.v(message, throwable)
        }
    }

    override fun d(message: String, throwable: Throwable?) {
        if (BuildConfig.DEBUG) {
            delegate.d(message, throwable)
        }
    }

    override fun i(message: String, throwable: Throwable?) {
        if (BuildConfig.DEBUG) {
            delegate.i(message, throwable)
        }
    }

    override fun w(message: String, throwable: Throwable?) {
        if (BuildConfig.DEBUG) {
            delegate.w(message, throwable)
        }
    }

    override fun e(message: String, throwable: Throwable?) {
        if (BuildConfig.DEBUG) {
            delegate.e(message, throwable)
        }
    }
}
