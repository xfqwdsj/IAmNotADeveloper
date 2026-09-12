package top.ltfan.notdeveloper.xposed.hook

import android.os.Binder
import io.github.libxposed.api.XposedInterface
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.log.callingPackageNotFoundWhen
import top.ltfan.notdeveloper.provider.SystemServiceProvider
import top.ltfan.notdeveloper.service.SystemService
import top.ltfan.notdeveloper.util.Reflect
import top.ltfan.notdeveloper.xposed.Module
import java.lang.reflect.Method

/**
 * Installs the system-server side of the module: a hook on
 * `ActivityManagerService.getContentProvider` that serves the module's
 * own providers (the cross-process app list and the settings database).
 */
context(module: Module)
fun installSystemHooks(classLoader: ClassLoader) {
    val systemServiceProvider = SystemServiceProvider(SystemService(classLoader))

    val activityManagerServiceClass = Reflect.findClass(
        "com.android.server.am.ActivityManagerService",
        classLoader,
    )

    installAllMethods(activityManagerServiceClass, "getContentProvider") { _: Method ->
        XposedInterface.Hooker { chain ->
            handleGetContentProvider(chain, classLoader, systemServiceProvider)
        }
    }
}

private fun handleGetContentProvider(
    chain: XposedInterface.Chain,
    classLoader: ClassLoader,
    systemServiceProvider: SystemServiceProvider,
): Any? {
    val args = chain.args
    val argsOffset = when (args.size) {
        4 -> 0
        5 -> 1
        else -> {
            Log.debug.e("Unsupported getContentProvider signature, args size: ${args.size}")
            return chain.proceed()
        }
    }
    val name = args[1 + argsOffset] as String
    val registered = RegisteredProvider.entries.firstOrNull { it.authority == name }
        ?: return chain.proceed()

    val ams = chain.thisObject
    val callingUid = Binder.getCallingUid()
    val callingPackage = when (args.size) {
        4 -> Reflect.currentApplication().packageManager
            .getPackagesForUid(callingUid)?.firstOrNull() ?: run {
            Log callingPackageNotFoundWhen "getContentProvider($name)"
            return chain.proceed()
        }

        5 -> args[1] as String
        else -> return chain.proceed()
    }

    val caller = args[0] ?: return chain.proceed()
    val userId = args[2 + argsOffset] as Int
    val stable = args[3 + argsOffset] as Boolean

    val helper: Any = try {
        Reflect.getObjectField(ams, "mCpHelper") ?: ams
    } catch (e: Throwable) {
        Log.d("mCpHelper field not found, using ActivityManagerService directly", e)
        ams
    }

    val context = ContentProviderContext(
        ams = ams,
        helper = helper,
        caller = caller,
        callingPackage = callingPackage,
        callingUid = callingUid,
        userId = userId,
        stable = stable,
        classLoader = classLoader,
        name = name,
    )

    return when (val patch = with(context) { registered(systemServiceProvider) }) {
        ProviderPatch.Proceed -> chain.proceed()
        is ProviderPatch.Result -> patch.value
    }
}
