package top.ltfan.notdeveloper.service

import android.content.Context
import android.provider.Settings
import top.ltfan.notdeveloper.data.PackageInfoWrapper
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.data.UserInfoName
import top.ltfan.notdeveloper.data.wrapped
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.util.getUserId
import top.ltfan.notdeveloper.database.PackageInfo as DatabasePackageInfo
import top.ltfan.notdeveloper.database.ParcelablePackageInfo

/**
 * App-side access to user and package information.
 *
 * The app process can enumerate its own installed packages and the current
 * user directly, so this contract is served locally without a cross-process
 * service.
 */
interface SystemServiceClient {
    fun queryUsers(userIds: List<Int> = emptyList()): List<UserInfo>

    fun queryAppsByUserId(userIds: List<Int> = emptyList()): List<PackageInfoWrapper>

    fun queryAppsByInfo(databaseList: List<ParcelablePackageInfo>): List<PackageInfoWrapper>

    fun notifySettingChange(name: String, type: Int)

    fun queryUsers(vararg userId: Int) = queryUsers(userId.toList())

    fun queryUser(userId: Int) = queryUsers(listOf(userId)).firstOrNull()

    fun queryApps(vararg userId: Int) = queryAppsByUserId(userId.toList())

    fun queryApps(databaseList: List<DatabasePackageInfo>) =
        queryAppsByInfo(databaseList.map { ParcelablePackageInfo(it) })

    fun notifySettingChange(method: DetectionMethod.SettingsMethod) {
        notifySettingChange(
            method.settingKey,
            when (method.settingsClass) {
                Settings.Global::class.java -> 0
                Settings.System::class.java -> 1
                Settings.Secure::class.java -> 2
                else -> error("Unknown settings class: ${method.settingsClass}")
            },
        )
    }
}

private class LocalSystemService(private val context: Context) : SystemServiceClient {
    private val packageManager get() = context.packageManager

    private val currentUser: UserInfo
        get() = UserInfo(
            packageManager.getPackageInfo(context.packageName, 0).getUserId(),
            UserInfoName.Current,
            0,
        )

    override fun queryUsers(userIds: List<Int>): List<UserInfo> {
        val user = currentUser
        return if (userIds.isEmpty()) listOf(user) else listOf(user).filter { it.id in userIds }
    }

    override fun queryAppsByUserId(userIds: List<Int>): List<PackageInfoWrapper> =
        runCatching {
            packageManager.getInstalledPackages(0)
                .mapNotNull { info -> info.applicationInfo?.let { info.wrapped() } }
        }.getOrElse {
            Log.Android.e("Failed to query installed packages: ${it.message}", it)
            emptyList()
        }

    override fun queryAppsByInfo(databaseList: List<ParcelablePackageInfo>): List<PackageInfoWrapper> =
        databaseList.mapNotNull { entry ->
            runCatching { packageManager.getPackageInfo(entry.packageName, 0).wrapped() }.getOrNull()
        }

    override fun notifySettingChange(name: String, type: Int) {
        Log.d("Settings change for $name (type $type) is applied in-process")
    }
}

val Context.systemService: SystemServiceClient get() = LocalSystemService(this)
