package top.ltfan.notdeveloper.service

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Binder
import android.os.Bundle
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.github.kr328.kaidl.BinderInterface
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.data.PackageInfoWrapper
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.data.UserInfoName
import top.ltfan.notdeveloper.data.wrapped
import top.ltfan.notdeveloper.database.PackageInfo as DatabasePackageInfo
import top.ltfan.notdeveloper.database.ParcelablePackageInfo
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.log.callingPackageNotFoundWhen
import top.ltfan.notdeveloper.log.invalidPackage
import top.ltfan.notdeveloper.provider.SystemServiceProvider
import top.ltfan.notdeveloper.provider.getInterfaceOrNull
import top.ltfan.notdeveloper.util.Reflect
import top.ltfan.notdeveloper.util.clearBinderCallingIdentity
import top.ltfan.notdeveloper.util.getUserId

const val CallMethodNotify = "NOTIFY"
const val BundleExtraType = "type"

/**
 * Cross-process contract served from system server, which can enumerate every
 * user and their packages on behalf of the module app.
 */
@BinderInterface
interface SystemServiceInterface {
    fun queryUsers(userIds: List<Int> = emptyList()): List<UserInfo>
    fun queryAppsByUserId(userIds: List<Int> = emptyList()): List<PackageInfoWrapper>
    fun queryAppsByInfo(databaseList: List<ParcelablePackageInfo>): List<PackageInfoWrapper>
    fun notifySettingChange(name: String, type: Int)
}

class SystemService(private val classLoader: ClassLoader) : SystemServiceInterface {
    override fun queryUsers(userIds: List<Int>): List<UserInfo> {
        val identity =
            "${SystemService::class.qualifiedName}.${::queryUsers.name}(${userIds.joinToString()})"
        val application = callingApplication(identity) ?: return emptyList()

        return clearBinderCallingIdentity {
            val userManager = application.getSystemService<UserManager>() ?: return@clearBinderCallingIdentity emptyList()
            (Reflect.callMethod(userManager, "getUsers") as List<*>)
                .map { user ->
                    UserInfo(
                        Reflect.getObjectField(user!!, "id") as Int,
                        Reflect.getObjectField(user, "name") as String?,
                        Reflect.getObjectField(user, "flags") as Int,
                    )
                }
                .let { list -> if (userIds.isNotEmpty()) list.filter { it.id in userIds } else list }
        }
    }

    override fun queryAppsByUserId(userIds: List<Int>): List<PackageInfoWrapper> {
        val identity =
            "${SystemService::class.qualifiedName}.${::queryAppsByUserId.name}(${userIds.joinToString()})"
        callingApplication(identity) ?: return emptyList()

        val packageManager = packageManager()
        val requestedIds = userIds.ifEmpty { queryUsers().map { it.id } }

        return requestedIds.asSequence().flatMap { userId ->
            clearBinderCallingIdentity {
                val slice = Reflect.callMethod(packageManager, "getInstalledPackages", 0, userId)
                @Suppress("UNCHECKED_CAST")
                Reflect.getObjectField(slice!!, "mList") as List<PackageInfo>
            }
        }.filter {
            val result = it.applicationInfo != null
            if (!result) {
                Log.w("Application info for package ${it.packageName} is null, skipping")
            }
            result
        }.map { it.wrapped() }.toList()
    }

    override fun queryAppsByInfo(databaseList: List<ParcelablePackageInfo>): List<PackageInfoWrapper> {
        val identity =
            "${SystemService::class.qualifiedName}.${::queryAppsByInfo.name}(${databaseList.joinToString()})"
        callingApplication(identity) ?: return emptyList()

        val packageManager = packageManager()

        return buildList {
            databaseList.forEach { (packageName, userId, appId) ->
                val info = clearBinderCallingIdentity {
                    Reflect.callMethod(
                        packageManager, "getPackageInfo",
                        packageName, 0, userId,
                    ) as? PackageInfo? ?: return@forEach
                }

                if (info.applicationInfo == null) {
                    Log.w("Application info for $packageName is null, skipping")
                    return@forEach
                }

                val queriedAppId = Reflect.callStaticMethod(
                    UserHandle::class.java, "getAppId",
                    info.applicationInfo?.uid,
                ) as? Int ?: return@forEach

                if (appId != queriedAppId) {
                    Log.i("App ID mismatch for package $packageName: expected $appId, got $queriedAppId")
                    return@forEach
                }

                add(info.wrapped())
            }
        }
    }

    override fun notifySettingChange(name: String, type: Int) {
        val identity =
            "${SystemService::class.qualifiedName}.${::notifySettingChange.name}($name, $type)"
        val application = callingApplication(identity) ?: return

        Log.d("Received notification request for $name")

        val uid = Binder.getCallingUid()
        val userId = Reflect.callStaticMethod(UserHandle::class.java, "getUserId", uid) as Int

        val bundle = Bundle().apply {
            putInt(BundleExtraType, type)
            putInt("_user", userId)
        }

        clearBinderCallingIdentity {
            application.contentResolver.call(
                "content://settings".toUri(),
                CallMethodNotify,
                name,
                bundle,
            )
        }

        Log.d("Requested notification for $name with type $type, user ID: $userId")
    }

    private fun callingApplication(identity: String): android.app.Application? {
        val application = Reflect.currentApplication()
        val uid = Binder.getCallingUid()
        val packageName = application.packageManager.getPackagesForUid(uid)
            ?.takeIf { it.size == 1 }?.first() ?: run {
            Log callingPackageNotFoundWhen identity
            return null
        }

        if (packageName != BuildConfig.APPLICATION_ID) {
            Log invalidPackage packageName skipping identity
            return null
        }
        return application
    }

    private fun packageManager(): Any {
        val serviceManagerClass = Reflect.findClass("android.os.ServiceManager", classLoader)
        val packageManagerService =
            Reflect.callStaticMethod(serviceManagerClass, "getService", "package")
        val stubClass = Reflect.findClass(
            "android.content.pm.IPackageManager\$Stub", classLoader,
        )
        return Reflect.callStaticMethod(stubClass, "asInterface", packageManagerService)!!
    }
}

val SystemServiceInterface.client inline get() = SystemServiceClient(this)

interface SystemServiceClient : SystemServiceInterface {
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

fun SystemServiceClient(service: SystemServiceInterface): SystemServiceClient =
    object : SystemServiceClient, SystemServiceInterface by service {}

/**
 * In-process fallback used while the system server service is not reachable.
 * It serves the current user and its installed packages.
 */
class LocalSystemService(private val context: Context) : SystemServiceClient {
    override fun queryUsers(userIds: List<Int>): List<UserInfo> {
        val user = UserInfo(
            context.packageManager.getPackageInfo(context.packageName, 0).getUserId(),
            UserInfoName.Current,
            0,
        )
        return if (userIds.isEmpty()) listOf(user) else listOf(user).filter { it.id in userIds }
    }

    override fun queryAppsByUserId(userIds: List<Int>): List<PackageInfoWrapper> = runCatching {
        context.packageManager.getInstalledPackages(0)
            .mapNotNull { info -> info.applicationInfo?.let { info.wrapped() } }
    }.getOrElse {
        Log.Android.e("Failed to query installed packages: ${it.message}", it)
        emptyList()
    }

    override fun queryAppsByInfo(databaseList: List<ParcelablePackageInfo>): List<PackageInfoWrapper> =
        databaseList.mapNotNull { entry ->
            runCatching { context.packageManager.getPackageInfo(entry.packageName, 0).wrapped() }
                .getOrNull()
        }

    override fun notifySettingChange(name: String, type: Int) {
        Log.d("Settings change for $name (type $type) is applied in-process")
    }
}

val Context.systemService: SystemServiceClient
    get() = runCatching {
        contentResolver.getInterfaceOrNull(SystemServiceProvider) {
            it.unwrap(SystemServiceInterface::class).client
        } ?: error("Failed to get SystemService binder")
    }.getOrElse {
        Log.Android.e("Failed to get SystemService: ${it.message}", it)
        LocalSystemService(this)
    }
