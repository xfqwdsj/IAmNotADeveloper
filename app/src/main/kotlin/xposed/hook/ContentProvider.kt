package top.ltfan.notdeveloper.xposed.hook

import android.content.ContentProvider
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.ltfan.notdeveloper.provider.BinderProvider
import top.ltfan.notdeveloper.provider.PackageSettingsDaoProvider
import top.ltfan.notdeveloper.xposed.NotDevServiceProvider

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
    block: context(ContentProviderContext) () -> R,
) = with(
    ContentProviderContext(
        ams, helper, caller, callingPackage, callingUid, userId, stable, lpparam, param
    ),
    block,
)

sealed class ContextProviderParameter {
    data object Inject : ContextProviderParameter()
    data class Provider(val provider: ContentProvider) : ContextProviderParameter()
}

enum class RegisteredProvider(val authority: String) {
    NotDevService(NotDevServiceProvider) {
        context(context: ContentProviderContext)
        override operator fun invoke(parameter: ContextProviderParameter) {
            if (parameter !is ContextProviderParameter.Provider) error("NotDevServiceProvider requires a ContentProvider parameter")
            val provider = parameter.provider
            val (ams, helper, caller, callingPackage, callingUid, userId, stable, lpparam, param) = context
            NotDevServiceProvider.patch(
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

    PackageSettingsDao(PackageSettingsDaoProvider) {
        context(context: ContentProviderContext)
        override operator fun invoke(parameter: ContextProviderParameter) {
            val (_, _, _, callingPackage, _, _, _, _, param) = context
            PackageSettingsDaoProvider.patch(callingPackage, param)
        }
    };

    constructor(provider: BinderProvider.Companion) : this(provider.authority)

    context(context: ContentProviderContext)
    abstract operator fun invoke(parameter: ContextProviderParameter = ContextProviderParameter.Inject)

    context(context: ContentProviderContext)
    operator fun invoke(provider: ContentProvider) {
        invoke(ContextProviderParameter.Provider(provider))
    }
}
