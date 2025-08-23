package top.ltfan.notdeveloper.service

import android.app.AndroidAppHelper
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
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.data.PackageInfoWrapper
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.data.wrapped
import top.ltfan.notdeveloper.database.ParcelablePackageInfo
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.log.callingPackageNotFoundWhen
import top.ltfan.notdeveloper.log.invalidPackage
import top.ltfan.notdeveloper.provider.SystemServiceProvider
import top.ltfan.notdeveloper.provider.getInterfaceOrNull
import top.ltfan.notdeveloper.util.clearBinderCallingIdentity
import top.ltfan.notdeveloper.database.PackageInfo as DatabasePackageInfo

const val CallMethodNotify = "NOTIFY"
const val BundleExtraType = "type"

@BinderInterface
interface SystemServiceInterface {
    fun queryUsers(userIds: List<Int> = emptyList()): List<UserInfo>
    fun queryAppsByUserId(userIds: List<Int> = emptyList()): List<PackageInfoWrapper>
    fun queryAppsByInfo(databaseList: List<ParcelablePackageInfo>): List<PackageInfoWrapper>
    fun notifySettingChange(name: String, type: Int)
}

class SystemService(private val lpparam: XC_LoadPackage.LoadPackageParam) : SystemServiceInterface {
    override fun queryUsers(userIds: List<Int>): List<UserInfo> {
        val identity =
            "${SystemService::class.qualifiedName}.${::queryUsers.name}(${userIds.joinToString()})"
        val application = AndroidAppHelper.currentApplication()
        val uid = Binder.getCallingUid()
        val packageName = application.packageManager.getPackagesForUid(uid)
            ?.takeIf { it.size == 1 }?.first() ?: run {
            Log callingPackageNotFoundWhen identity
            return emptyList()
        }

        if (packageName != BuildConfig.APPLICATION_ID) {
            Log invalidPackage packageName skipping identity
            return emptyList()
        }

        return clearBinderCallingIdentity {
            val userManager = application.getSystemService<UserManager>()
            (XposedHelpers.callMethod(userManager, "getUsers") as List<*>)
                .map {
                    val id = XposedHelpers.getIntField(it, "id")
                    UserInfo(
                        id,
                        XposedHelpers.getObjectField(it, "name") as String?,
                        XposedHelpers.getIntField(it, "flags"),
                    )
                }
                .let { list -> if (userIds.isNotEmpty()) list.filter { it.id in userIds } else list }
        }
    }

    override fun queryAppsByUserId(userIds: List<Int>): List<PackageInfoWrapper> {
        val identity =
            "${SystemService::class.qualifiedName}.${::queryAppsByUserId.name}(${userIds.joinToString()})"
        val application = AndroidAppHelper.currentApplication()
        val uid = Binder.getCallingUid()
        val packageName =
            application.packageManager.getPackagesForUid(uid)
                ?.takeIf { it.size == 1 }?.first() ?: run {
                Log callingPackageNotFoundWhen identity
                return emptyList()
            }

        if (packageName != BuildConfig.APPLICATION_ID) {
            Log invalidPackage packageName skipping identity
            return emptyList()
        }

        val serviceManagerClass = XposedHelpers.findClass(
            "android.os.ServiceManager", lpparam.classLoader
        )
        val packageManagerService = XposedHelpers.callStaticMethod(
            serviceManagerClass, "getService", "package"
        )
        val iPackageManagerStubClass = XposedHelpers.findClass(
            "android.content.pm.IPackageManager\$Stub", lpparam.classLoader
        )
        val packageManager = XposedHelpers.callStaticMethod(
            iPackageManagerStubClass, "asInterface", packageManagerService
        )

        val requestedIds = userIds.ifEmpty { queryUsers().map { it.id } }

        return requestedIds.asSequence().flatMap {
            clearBinderCallingIdentity {
                val slice = XposedHelpers.callMethod(
                    packageManager, "getInstalledPackages",
                    0, it,
                )
                @Suppress("UNCHECKED_CAST")
                XposedHelpers.getObjectField(slice, "mList") as List<PackageInfo>
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
        val application = AndroidAppHelper.currentApplication()
        val uid = Binder.getCallingUid()
        val packageName =
            application.packageManager.getPackagesForUid(uid)
                ?.takeIf { it.size == 1 }?.first() ?: run {
                Log callingPackageNotFoundWhen identity
                return emptyList()
            }

        if (packageName != BuildConfig.APPLICATION_ID) {
            Log invalidPackage packageName skipping identity
            return emptyList()
        }

        val serviceManagerClass = XposedHelpers.findClass(
            "android.os.ServiceManager", lpparam.classLoader
        )
        val packageManagerService = XposedHelpers.callStaticMethod(
            serviceManagerClass, "getService", "package"
        )
        val iPackageManagerStubClass = XposedHelpers.findClass(
            "android.content.pm.IPackageManager\$Stub", lpparam.classLoader
        )
        val packageManager = XposedHelpers.callStaticMethod(
            iPackageManagerStubClass, "asInterface", packageManagerService
        )

        return buildList {
            databaseList.forEach { (packageName, userId, appId) ->
                val info = clearBinderCallingIdentity {
                    XposedHelpers.callMethod(
                        packageManager, "getPackageInfo",
                        packageName, 0, userId,
                    ) as? PackageInfo? ?: return@forEach
                }

                if (info.applicationInfo == null) {
                    Log.w("Application info for $packageName is null, skipping")
                    return@forEach
                }

                val queriedAppId = XposedHelpers.callStaticMethod(
                    UserHandle::class.java, "getAppId",
                    info.applicationInfo?.uid,
                ) as? Int? ?: return@forEach

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
        val application = AndroidAppHelper.currentApplication()
        val uid = Binder.getCallingUid()
        val packageName =
            application.packageManager.getPackagesForUid(uid)
                ?.takeIf { it.size == 1 }?.first() ?: run {
                Log callingPackageNotFoundWhen identity
                return
            }

        if (packageName != BuildConfig.APPLICATION_ID) {
            Log invalidPackage packageName skipping identity
            return
        }

        Log.d("Received notification request for $name")

        val userId = XposedHelpers.callStaticMethod(
            UserHandle::class.java, "getUserId", uid
        ) as Int

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

        Log.d("Requested notification for $name with type $type from package $packageName, user ID: $userId")
    }
}

val SystemServiceInterface.client inline get() = SystemServiceClient(this)

interface SystemServiceClient : SystemServiceInterface {
    fun queryUsers(vararg userId: Int) = queryUsers(userId.toList())
    fun queryUser(userId: Int) = queryUsers(listOf(userId)).firstOrNull()
    fun queryApps(vararg userId: Int) = queryAppsByUserId(userId.toList())
    fun queryApps(databaseList: List<DatabasePackageInfo>) =
        queryAppsByInfo(databaseList.map { ParcelablePackageInfo(it) })

    fun notifySettingChange(method: DetectionMethod.SettingsMethod)
}

fun SystemServiceClient(service: SystemServiceInterface): SystemServiceClient =
    object : SystemServiceClient, SystemServiceInterface by service {
        override fun notifySettingChange(method: DetectionMethod.SettingsMethod) {
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

val Context.systemService
    get() = runCatching {
        contentResolver.getInterfaceOrNull(SystemServiceProvider) {
            it.unwrap(SystemServiceInterface::class).client
        } ?: error("Failed to get SystemService binder")
    }.getOrElse {
        Log.Android.e("Failed to get SystemService: ${it.message}", it)
        null
    }
