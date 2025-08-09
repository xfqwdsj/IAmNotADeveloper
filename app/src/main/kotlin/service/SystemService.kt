package top.ltfan.notdeveloper.service

import android.content.Context
import android.content.pm.ApplicationInfo
import android.provider.Settings
import com.github.kr328.kaidl.BinderInterface
import top.ltfan.dslutilities.LockableValueDsl
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.provider.SystemServiceProvider
import top.ltfan.notdeveloper.provider.getInterfaceOrNull

const val CallMethodNotify = "NOTIFY"
const val BundleExtraType = "type"

@BinderInterface
interface SystemServiceInterface {
    suspend fun queryApps(userId: Int? = null): List<ApplicationInfo>
    suspend fun notifySettingChange(name: String, type: Int)
}

suspend fun SystemServiceInterface.notifySettingChange(method: DetectionMethod.SettingsMethod) {
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

open class SystemServiceClient(service: SystemServiceInterface) : SystemServiceInterface by service

@SystemServiceBuilder.Dsl
class SystemServiceBuilder : LockableValueDsl() {
    var queryApps by required<suspend SystemServiceInterface.(userId: Int?) -> List<ApplicationInfo>>()
    var notifySettingChange by required<suspend SystemServiceInterface.(name: String, type: Int) -> Unit>()

    fun queryApps(block: suspend SystemServiceInterface.(userId: Int?) -> List<ApplicationInfo>) {
        queryApps = block
    }

    fun notifySettingChange(block: suspend SystemServiceInterface.(name: String, type: Int) -> Unit) {
        notifySettingChange = block
    }

    fun build(): SystemServiceInterface {
        lock()
        return object : SystemServiceInterface {
            override suspend fun queryApps(userId: Int?) = queryApps.invoke(this, userId)
            override suspend fun notifySettingChange(name: String, type: Int) =
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
