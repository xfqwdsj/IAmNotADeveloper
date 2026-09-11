package top.ltfan.notdeveloper.xposed

import io.github.libxposed.api.XposedModule
import top.ltfan.notdeveloper.BuildConfig

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

    val debug get() = DebugLogger(this)
}

interface XposedLogger {
    context(module: XposedModule)
    fun v(message: String, throwable: Throwable? = null) {
        module.log(android.util.Log.VERBOSE, LogTag, message, throwable)
    }

    context(module: XposedModule)
    fun d(message: String, throwable: Throwable? = null) {
        module.log(android.util.Log.DEBUG, LogTag, message, throwable)
    }

    context(module: XposedModule)
    fun i(message: String, throwable: Throwable? = null) {
        module.log(android.util.Log.INFO, LogTag, message, throwable)
    }

    context(module: XposedModule)
    fun w(message: String, throwable: Throwable? = null) {
        module.log(android.util.Log.WARN, LogTag, message, throwable)
    }

    context(module: XposedModule)
    fun e(message: String, throwable: Throwable? = null) {
        module.log(android.util.Log.ERROR, LogTag, message, throwable)
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
