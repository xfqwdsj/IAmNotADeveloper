package top.ltfan.notdeveloper.data

import android.content.pm.PackageInfo
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.parcelableCreator
import top.ltfan.notdeveloper.util.getAppId
import top.ltfan.notdeveloper.util.getUserId
import top.ltfan.notdeveloper.database.PackageInfo as DatabaseInfo

@Parcelize
data class PackageInfoWrapper
@Deprecated("Do not use this") constructor(val info: PackageInfo) : Parcelable {
    companion object {
        val CREATOR = parcelableCreator<PackageInfoWrapper>()
    }

    fun toDatabaseInfo() = DatabaseInfo(
        packageName = info.packageName,
        userId = info.getUserId(),
        appId = info.getAppId(),
    )

    override fun equals(other: Any?): Boolean {
        val info = when (other) {
            is PackageInfoWrapper -> other.info
            is PackageInfo -> other
            else -> return false
        }
        return this.info.packageName == info.packageName && this.info.applicationInfo?.uid == info.applicationInfo?.uid
    }

    override fun hashCode(): Int {
        return 31 * info.packageName.hashCode() + (info.applicationInfo?.uid?.hashCode() ?: 0)
    }
}

@Suppress("DEPRECATION")
fun PackageInfo.wrapped() = PackageInfoWrapper(this)
