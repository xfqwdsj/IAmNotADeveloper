package top.ltfan.notdeveloper.application

import android.app.Application
import top.ltfan.notdeveloper.database.PackageSettingsDatabase

class NotDevApplication : Application() {
    val database by lazy { PackageSettingsDatabase.get() }
}
