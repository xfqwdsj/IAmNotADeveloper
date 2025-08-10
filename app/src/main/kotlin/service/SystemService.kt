package top.ltfan.notdeveloper.service

import android.content.Context
import android.content.pm.ApplicationInfo
import android.provider.Settings
import com.github.kr328.kaidl.BinderInterface
import top.ltfan.dslutilities.LockableValueDsl
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.provider.SystemServiceProvider
import top.ltfan.notdeveloper.provider.getInterfaceOrNull

const val CallMethodNotify = "NOTIFY"
const val BundleExtraType = "type"

@BinderInterface
interface SystemServiceInterface {
    fun queryUsers(userIds: List<Int>? = null): List<UserInfo>
    fun queryApps(userIds: List<Int>? = null): List<ApplicationInfo>
    fun notifySettingChange(name: String, type: Int)
}

fun SystemServiceInterface.notifySettingChange(method: DetectionMethod.SettingsMethod) {
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

val SystemServiceInterface.client inline get() = SystemServiceClient(this)

interface SystemServiceClient : SystemServiceInterface {
    fun queryUsers(vararg userId: Int) = queryUsers(userId.toList())
    fun queryUser(userId: Int) = queryUsers(listOf(userId)).firstOrNull()
    fun queryApps(vararg userId: Int) = queryApps(userId.toList())
}

fun SystemServiceClient(service: SystemServiceInterface): SystemServiceClient =
    object : SystemServiceClient, SystemServiceInterface by service {}

@SystemServiceBuilder.Dsl
class SystemServiceBuilder : LockableValueDsl() {
    var queryUsers by required<SystemServiceInterface.(userIds: List<Int>?) -> List<UserInfo>>()
    var queryApps by required<SystemServiceInterface.(userIds: List<Int>?) -> List<ApplicationInfo>>()
    var notifySettingChange by required<SystemServiceInterface.(name: String, type: Int) -> Unit>()

    fun queryUsers(block: SystemServiceInterface.(userIds: List<Int>?) -> List<UserInfo>) {
        queryUsers = block
    }

    fun queryApps(block: SystemServiceInterface.(userIds: List<Int>?) -> List<ApplicationInfo>) {
        queryApps = block
    }

    fun notifySettingChange(block: SystemServiceInterface.(name: String, type: Int) -> Unit) {
        notifySettingChange = block
    }

    fun build(): SystemServiceInterface {
        lock()
        return object : SystemServiceInterface {
            override fun queryUsers(userIds: List<Int>?) = queryUsers.invoke(this, userIds)
            override fun queryApps(userIds: List<Int>?) = queryApps.invoke(this, userIds)
            override fun notifySettingChange(name: String, type: Int) =
                notifySettingChange.invoke(this, name, type)
        }
    }

    companion object {
        inline fun build(block: SystemServiceBuilder.() -> Unit): SystemServiceInterface =
            SystemServiceBuilder().apply(block).build()
    }

    @DslMarker
    annotation class Dsl
}

@Suppress("FunctionName")
inline fun SystemService(block: SystemServiceBuilder.() -> Unit): SystemServiceInterface =
    SystemServiceBuilder.build(block)

val Context.systemService
    get() = runCatching {
        contentResolver.getInterfaceOrNull(SystemServiceProvider) {
            it.unwrap(SystemServiceInterface::class).client
        } ?: error("Failed to get SystemService binder")
    }.getOrElse {
        Log.Android.e("Failed to get SystemService: ${it.message}", it)
        null
    }
