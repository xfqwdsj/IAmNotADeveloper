package top.ltfan.notdeveloper.provider

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import androidx.core.net.toUri

const val CallMethodGet = "GET"
const val BundleExtraService = "service"

abstract class BinderProvider : ContentProvider() {
    protected abstract val binder: IBinder

    interface Companion {
        val authority: String
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (method != CallMethodGet) return null
        return Bundle().apply {
            putBinder(BundleExtraService, binder)
        }
    }

    override fun onCreate() = true
    override fun getType(uri: Uri) = null

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String?>?
    ) = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String?>?
    ) = 0

    override fun query(
        uri: Uri,
        projection: Array<out String?>?,
        selection: String?,
        selectionArgs: Array<out String?>?,
        sortOrder: String?
    ): Cursor? = null
}

inline fun <R> ContentResolver.getInterfaceOrNull(
    provider: BinderProvider.Companion,
    builder: (IBinder) -> R,
) = getInterfaceOrNull(
    provider.authority,
    builder,
)

inline fun <R> ContentResolver.getInterfaceOrNull(authority: String, builder: (IBinder) -> R) =
    getInterfaceOrNull(
        "content://$authority".toUri(),
        builder,
    )

inline fun <R> ContentResolver.getInterfaceOrNull(
    uri: Uri,
    builder: (IBinder) -> R,
) = call(uri, CallMethodGet, null, null)
    ?.getBinder(BundleExtraService)?.let(builder)
