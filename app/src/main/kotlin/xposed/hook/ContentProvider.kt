package top.ltfan.notdeveloper.xposed.hook

import android.content.ContentProvider
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.ltfan.notdeveloper.provider.BinderProvider
import top.ltfan.notdeveloper.provider.DatabaseServiceProvider
import top.ltfan.notdeveloper.provider.SystemServiceProvider

data class ContentProviderContext(
    val ams: Any,
    val helper: Any,
    val caller: Any,
    val callingPackage: String,
    val callingUid: Int,
    val userId: Int,
    val stable: Boolean,
    val lpparam: XC_LoadPackage.LoadPackageParam,
    val param: XC_MethodHook.MethodHookParam,
    val name: String,
)

inline fun <R> withContentProviderContext(
    ams: Any,
    helper: Any,
    caller: Any,
    callingPackage: String,
    callingUid: Int,
    userId: Int,
    stable: Boolean,
    lpparam: XC_LoadPackage.LoadPackageParam,
    param: XC_MethodHook.MethodHookParam,
    name: String,
    block: context(ContentProviderContext) () -> R,
) = with(
    ContentProviderContext(
        ams, helper, caller, callingPackage, callingUid, userId, stable, lpparam, param, name
    ),
    block,
)

sealed class ContextProviderParameter {
    data object Inject : ContextProviderParameter()
    data class Provider(val provider: ContentProvider) : ContextProviderParameter()
}

enum class RegisteredProvider(val authority: String) {
    SystemService(SystemServiceProvider) {
        context(context: ContentProviderContext)
        override fun patch(parameter: ContextProviderParameter) {
            if (parameter !is ContextProviderParameter.Provider) error("SystemServiceProvider requires a ContentProvider parameter")
            val provider = parameter.provider
            val (ams, helper, caller, callingPackage, callingUid, userId, stable, lpparam, param) = context
            SystemServiceProvider.patch(
                provider,
                ams,
                helper,
                caller,
                callingPackage,
                callingUid,
                userId,
                stable,
                lpparam,
                param
            )
        }
    },

    DatabaseService(DatabaseServiceProvider) {
        context(context: ContentProviderContext)
        override fun patch(parameter: ContextProviderParameter) {
            val (_, _, _, callingPackage, _, _, _, _, param) = context
            DatabaseServiceProvider.patch(callingPackage, param)
        }
    };

    constructor(provider: BinderProvider.Companion) : this(provider.authority)

    context(context: ContentProviderContext)
    abstract fun patch(parameter: ContextProviderParameter)

    context(context: ContentProviderContext)
    operator fun invoke(parameter: ContextProviderParameter = ContextProviderParameter.Inject) {
        val name = context.name
        if (name != authority) return
        patch(parameter)
    }

    context(context: ContentProviderContext)
    operator fun invoke(provider: ContentProvider) {
        invoke(ContextProviderParameter.Provider(provider))
    }
}
