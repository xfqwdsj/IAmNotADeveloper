package xyz.xfqlittlefan.notdeveloper

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build

val mode
    @SuppressLint("WorldReadableFiles")
    inline get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Context.MODE_WORLD_READABLE else Context.MODE_PRIVATE

const val DEVELOPMENT_SETTINGS_ENABLED = "development_settings_enabled"
const val ADB_ENABLED = "adb_enabled"
const val ADB_WIFI_ENABLED = "adb_wifi_enabled"