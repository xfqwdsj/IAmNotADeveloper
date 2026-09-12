package top.ltfan.notdeveloper.application

import top.ltfan.notdeveloper.App
import top.ltfan.notdeveloper.database.PackageSettingsDatabase

class NotDevApplication : App() {
    val database by lazy { PackageSettingsDatabase.get() }
}
