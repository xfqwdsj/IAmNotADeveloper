package top.ltfan.notdeveloper.log

infix fun Logger.callingPackageNotFoundWhen(what: String?) {
    e("Calling package not found when $what")
}

class InvalidPackageLogger(
    val callingPackage: String?,
    val logger: Logger,
) {
    infix fun log(what: String?) {
        logger.e("Invalid package $callingPackage$what")
    }

    infix fun requesting(what: String?) {
        log(" requesting $what")
    }
}

infix fun Logger.invalidPackage(callingPackage: String?) =
    InvalidPackageLogger(callingPackage, this)

data class LoggerProcessingScope(var logAppendix: String = "")

inline fun <R> Logger.processing(what: String?, action: LoggerProcessingScope.() -> R): R {
    d("Processing $what")
    val scope = LoggerProcessingScope()
    return scope.action().also {
        d("Finished processing $what${scope.logAppendix}")
    }
}

infix fun Logger.notPreferredHook(what: String?) {
    d("Skipping $what because hook is not preferred")
}

infix fun Logger.disabledHook(what: String?) {
    d("Skipping $what because hook is disabled")
}
