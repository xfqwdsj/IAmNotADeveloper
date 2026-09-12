package top.ltfan.notdeveloper.xposed.hook

import android.content.ContentProvider
import top.ltfan.notdeveloper.provider.DatabaseServiceProvider
import top.ltfan.notdeveloper.provider.SystemServiceProvider

/**
 * How the system server should answer a `getContentProvider` call that
 * names a provider registered by this module.
 */
sealed interface ProviderPatch {
    /** Let the framework resolve the provider as usual. */
    data object Proceed : ProviderPatch

    /** Return [value] to the caller instead of letting the framework resolve. */
    data class Result(val value: Any?) : ProviderPatch
}

data class ContentProviderContext(
    val ams: Any,
    val helper: Any,
    val caller: Any,
    val callingPackage: String,
    val callingUid: Int,
    val userId: Int,
    val stable: Boolean,
    val classLoader: ClassLoader,
    val name: String,
)

inline fun <R> withContentProviderContext(
    context: ContentProviderContext,
    block: context(ContentProviderContext) () -> R,
): R = with(context, block)

enum class RegisteredProvider(val authority: String) {
    SystemService(SystemServiceProvider) {
        context(context: ContentProviderContext)
        override fun patch(provider: ContentProvider?): ProviderPatch {
            val provider = provider ?: return ProviderPatch.Result(null)
            val (ams, helper, caller, callingPackage, callingUid, userId, stable, classLoader) =
                context
            return ProviderPatch.Result(
                SystemServiceProvider.patch(
                    provider,
                    ams,
                    helper,
                    caller,
                    callingPackage,
                    callingUid,
                    userId,
                    stable,
                    classLoader,
                ),
            )
        }
    },

    DatabaseService(DatabaseServiceProvider) {
        context(context: ContentProviderContext)
        override fun patch(provider: ContentProvider?): ProviderPatch =
            if (DatabaseServiceProvider.isAllowed(context.callingPackage)) {
                ProviderPatch.Proceed
            } else {
                ProviderPatch.Result(null)
            }
    };

    constructor(provider: top.ltfan.notdeveloper.provider.BinderProvider.Companion) :
            this(provider.authority)

    context(context: ContentProviderContext)
    abstract fun patch(provider: ContentProvider?): ProviderPatch

    context(context: ContentProviderContext)
    operator fun invoke(provider: ContentProvider? = null): ProviderPatch {
        if (context.name != authority) return ProviderPatch.Proceed
        return patch(provider)
    }
}
