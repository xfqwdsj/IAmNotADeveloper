package top.ltfan.notdeveloper.provider

import android.os.IBinder
import androidx.room.Room
import de.robv.android.xposed.XC_MethodHook
import top.ltfan.notdeveloper.database.PackageSettingsDatabase
import top.ltfan.notdeveloper.service.DatabaseService
import top.ltfan.notdeveloper.service.wrap
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.log.invalidPackage

class DatabaseServiceProvider : BinderProvider() {
    private var _binder: IBinder? = null

    override fun onCreate(): Boolean {
        val application = context!!.applicationContext
        val database = Room.databaseBuilder(
            application,
            PackageSettingsDatabase::class.java,
            PackageSettingsDatabase.DATABASE_NAME,
        ).build()
        _binder = DatabaseService(database.dao()).wrap()
        return true
    }

    override val binder: IBinder by lazy { _binder!! }

    companion object : BinderProvider.Companion {
        override val authority = "notdevsettings"
        val validPackages = listOf("android", "com.android.providers.settings")

        fun patch(callingPackage: String, param: XC_MethodHook.MethodHookParam) {
            if (callingPackage !in validPackages) {
                Log invalidPackage callingPackage requesting DatabaseServiceProvider::class.qualifiedName
                param.result = null
            }
        }
    }
}
