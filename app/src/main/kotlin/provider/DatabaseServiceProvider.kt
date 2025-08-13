package top.ltfan.notdeveloper.provider

import android.os.IBinder
import de.robv.android.xposed.XC_MethodHook
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.log.invalidPackage
import top.ltfan.notdeveloper.service.DatabaseService
import top.ltfan.notdeveloper.service.wrap

class DatabaseServiceProvider : BinderProvider() {
    private var _binder: IBinder? = null

    override fun onCreate(): Boolean {
        _binder = DatabaseService(application.database.dao()).wrap()
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
