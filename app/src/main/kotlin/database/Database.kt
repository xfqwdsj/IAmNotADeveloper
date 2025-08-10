package top.ltfan.notdeveloper.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PackageInfo::class, Detection::class], version = 1)
abstract class PackageSettingsDatabase : RoomDatabase() {
    abstract fun dao(): PackageSettingsDao

    companion object {
        const val DATABASE_NAME = "package_settings.db"
    }
}
