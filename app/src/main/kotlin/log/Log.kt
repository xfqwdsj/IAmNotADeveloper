package top.ltfan.notdeveloper.log

import android.util.Log
import top.ltfan.notdeveloper.BuildConfig

const val LogTag = "NotDeveloper"

interface Logger {
    fun v(message: String?, throwable: Throwable? = null) {
        Log.v(LogTag, message, throwable)
    }

    fun d(message: String?, throwable: Throwable? = null) {
        Log.d(LogTag, message, throwable)
    }

    fun i(message: String?, throwable: Throwable? = null) {
        Log.i(LogTag, message, throwable)
    }

    fun w(message: String?, throwable: Throwable? = null) {
        Log.w(LogTag, message, throwable)
    }

    fun e(message: String?, throwable: Throwable? = null) {
        Log.e(LogTag, message, throwable)
    }

    val debug get() = DebugLogger(this)
}

object Log : Logger {
    object Android : Logger
}

class DebugLogger(private val delegate: Logger) : Logger by delegate {
    override fun v(message: String?, throwable: Throwable?) {
        if (BuildConfig.DEBUG) {
            delegate.v(message, throwable)
        }
    }

    override fun d(message: String?, throwable: Throwable?) {
        if (BuildConfig.DEBUG) {
            delegate.d(message, throwable)
        }
    }

    override fun i(message: String?, throwable: Throwable?) {
        if (BuildConfig.DEBUG) {
            delegate.i(message, throwable)
        }
    }

    override fun w(message: String?, throwable: Throwable?) {
        if (BuildConfig.DEBUG) {
            delegate.w(message, throwable)
        }
    }

    override fun e(message: String?, throwable: Throwable?) {
        if (BuildConfig.DEBUG) {
            delegate.e(message, throwable)
        }
    }
}
