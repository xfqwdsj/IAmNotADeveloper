package top.ltfan.notdeveloper.data

import android.content.pm.PackageInfo
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.parcelableCreator

@Parcelize
data class PackageInfoWrapper
@Deprecated("Do not use this") constructor(val info: PackageInfo) : Parcelable {
    companion object {
        val CREATOR = parcelableCreator<PackageInfoWrapper>()
    }

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
