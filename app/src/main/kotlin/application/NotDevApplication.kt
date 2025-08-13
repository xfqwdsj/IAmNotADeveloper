package top.ltfan.notdeveloper.application

import android.app.Application
import androidx.room.Room
import top.ltfan.notdeveloper.database.PackageSettingsDatabase

class NotDevApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(
            this,
            PackageSettingsDatabase::class.java,
            PackageSettingsDatabase.DATABASE_NAME,
        ).build()
    }

    override fun onCreate() {
        super.onCreate()
    }
}
