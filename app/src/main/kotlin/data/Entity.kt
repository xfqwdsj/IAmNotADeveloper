package top.ltfan.notdeveloper.data

import android.os.Parcel
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
    var packageName: String? = null,
    var userId: Int? = null,
    var appId: Int? = null,
) : Parcelable {
    companion object {
        @JvmStatic
        fun readFromParcel(source: Parcel): ParcelablePackageInfo =
            parcelableCreator<ParcelablePackageInfo>().createFromParcel(source)
    }

    constructor(packageInfo: PackageInfo) : this(
        packageName = packageInfo.packageName,
        userId = packageInfo.userId,
        appId = packageInfo.appId
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
