package top.ltfan.notdeveloper.provider

import android.os.IBinder
import androidx.room.Room
import de.robv.android.xposed.XC_MethodHook
import top.ltfan.notdeveloper.data.PackageSettingsDatabase
import top.ltfan.notdeveloper.xposed.PackageSettingsDaoService

class PackageSettingsDaoProvider : BinderProvider() {
    private var _binder: IBinder? = null

    override fun onCreate(): Boolean {
        val application = context!!.applicationContext
        val database = Room.databaseBuilder(
            application,
            PackageSettingsDatabase::class.java,
            PackageSettingsDatabase.DATABASE_NAME,
        ).build()
        _binder = PackageSettingsDaoService(database.dao()).asBinder()
        return true
    }

    override val binder: IBinder by lazy { _binder!! }

    companion object : BinderProvider.Companion {
        override val authority = "notdevsettings"
        val validPackages = listOf("android", "com.android.providers.settings")

        fun patch(callingPackage: String, param: XC_MethodHook.MethodHookParam) {
            if (callingPackage !in validPackages) {
                param.result = null
            }
        }
    }
}
