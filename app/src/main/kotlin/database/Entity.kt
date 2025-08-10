package top.ltfan.notdeveloper.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.parcelableCreator

@Entity(primaryKeys = ["packageName", "userId"])
data class PackageInfo(
    val packageName: String,
    val userId: Int,
    val appId: Int,
)

@Parcelize
data class ParcelablePackageInfo(
    var packageName: String,
    var userId: Int,
    var appId: Int,
) : Parcelable {
    companion object {
        val CREATOR: Parcelable.Creator<ParcelablePackageInfo> =
            parcelableCreator<ParcelablePackageInfo>()
    }

    constructor(packageInfo: PackageInfo) : this(
        packageName = packageInfo.packageName,
        userId = packageInfo.userId,
        appId = packageInfo.appId,
    )

    fun restore() = PackageInfo(
        packageName = packageName,
        userId = userId,
        appId = appId,
    )
}

@Entity(
    primaryKeys = ["packageName", "userId", "methodName"],
    foreignKeys = [
        ForeignKey(
            entity = PackageInfo::class,
            parentColumns = ["packageName", "userId"],
            childColumns = ["packageName", "userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Detection(
    val packageName: String,
    val userId: Int,
    val methodName: String,
    val enabled: Boolean,
)
