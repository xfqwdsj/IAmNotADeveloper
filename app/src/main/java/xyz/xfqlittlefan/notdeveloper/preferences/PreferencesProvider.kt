package xyz.xfqlittlefan.notdeveloper.preferences

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import xyz.xfqlittlefan.notdeveloper.BuildConfig

class PreferencesProvider : ContentProvider() {
    override fun onCreate() = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val cursor = MatrixCursor(arrayOf("value"))
        val preferences = context!!.getSharedPreferences(
            BuildConfig.APPLICATION_ID + "_preferences",
            Context.MODE_PRIVATE
        )
        uri.pathSegments.getOrNull(0)?.let {
            cursor.addRow(arrayOf(preferences.getBoolean(it, true)))
        }
        return cursor
    }

    override fun getType(uri: Uri) = "text/plain"

    override fun insert(uri: Uri, values: ContentValues?) = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ) = 0
}