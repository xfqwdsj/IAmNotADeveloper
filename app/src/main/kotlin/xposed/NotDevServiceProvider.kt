package top.ltfan.notdeveloper.xposed

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ProviderInfo
import android.database.AbstractCursor
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import top.ltfan.notdeveloper.service.INotDevService
import androidx.core.net.toUri

class NotDevServiceProvider(private val service: INotDevService) : ContentProvider() {
    companion object {
        val uri = "content://${NotDevServiceProvider::class.java.name}".toUri()
        val info = ProviderInfo().apply {
            authority = uri.authority
            name = NotDevServiceProvider::class.java.name
            exported = true
            grantUriPermissions = true
        }
    }

    override fun onCreate() = true
    override fun getType(uri: Uri) = null

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (method != CallMethodGet) return null
        return Bundle().apply {
            putBinder(BundleExtraService, service.asBinder())
        }
    }

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
