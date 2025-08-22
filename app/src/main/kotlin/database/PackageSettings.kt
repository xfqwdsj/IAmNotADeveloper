package top.ltfan.notdeveloper.database

import android.app.Application
import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.parcelableCreator
import top.ltfan.notdeveloper.detection.DetectionCategory

@Database(entities = [PackageInfo::class, Detection::class], version = 1)
abstract class PackageSettingsDatabase : RoomDatabase() {
    abstract fun dao(): PackageSettingsDao

    companion object {
        const val DATABASE_NAME = "package_settings.db"

        context(application: Application)
        fun get() = Room.databaseBuilder(
            application,
            PackageSettingsDatabase::class.java,
            DATABASE_NAME,
        ).build()
    }
}

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
        val CREATOR = parcelableCreator<ParcelablePackageInfo>()
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

@Dao
interface PackageSettingsDao {
    @Query("INSERT OR REPLACE INTO PackageInfo (packageName, userId, appId) VALUES (:packageName, :userId, :appId)")
    suspend fun insertPackageInfo(packageName: String, userId: Int, appId: Int)

    @Query("DELETE FROM PackageInfo WHERE packageName = :packageName AND userId = :userId")
    suspend fun deletePackageInfo(packageName: String, userId: Int)

    @Query("SELECT * FROM PackageInfo WHERE packageName = :packageName")
    suspend fun getPackageInfo(packageName: String): List<PackageInfo>

    @Query("SELECT * FROM PackageInfo WHERE packageName = :packageName")
    fun getPackageInfoFlow(packageName: String): Flow<List<PackageInfo>>

    @Query("SELECT * FROM PackageInfo WHERE userId = :userId")
    suspend fun getPackageInfo(userId: Int): List<PackageInfo>

    @Query("SELECT * FROM PackageInfo WHERE userId = :userId")
    fun getPackageInfoFlow(userId: Int): Flow<List<PackageInfo>>

    @Query("SELECT * FROM PackageInfo")
    fun getPackageInfoFlow(): Flow<List<PackageInfo>>

    @Query("SELECT EXISTS(SELECT 1 FROM PackageInfo WHERE packageName = :packageName AND userId = :userId)")
    suspend fun isPackageExists(packageName: String, userId: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM PackageInfo WHERE packageName = :packageName AND userId = :userId)")
    fun isPackageExistsFlow(packageName: String, userId: Int): Flow<Boolean>

    @Query("INSERT OR REPLACE INTO Detection (packageName, userId, methodName, enabled) VALUES (:packageName, :userId, :methodName, :enabled)")
    suspend fun insertDetection(
        packageName: String, userId: Int, methodName: String, enabled: Boolean
    )

    @Query("SELECT EXISTS(SELECT 1 FROM Detection WHERE packageName = :packageName AND userId = :userId AND methodName = :methodName)")
    suspend fun isDetectionSet(packageName: String, userId: Int, methodName: String): Boolean

    @Query("SELECT COALESCE((SELECT enabled FROM Detection WHERE packageName = :packageName AND userId = :userId AND methodName = :methodName), 1)")
    suspend fun isDetectionEnabled(packageName: String, userId: Int, methodName: String): Boolean

    @Query("SELECT COALESCE((SELECT enabled FROM Detection WHERE packageName = :packageName AND userId = :userId AND methodName = :methodName), 1)")
    fun isDetectionEnabledFlow(packageName: String, userId: Int, methodName: String): Flow<Boolean>

    @Query("DELETE FROM PackageInfo")
    suspend fun clearAllPackages()

    @Transaction
    suspend fun toggleDetectionEnabled(packageName: String, userId: Int, methodName: String) {
        val currentEnabled = isDetectionEnabled(packageName, userId, methodName)
        insertDetection(packageName, userId, methodName, !currentEnabled)
    }

    @Transaction
    suspend fun enableAllDetectionsForPackage(packageName: String, userId: Int) {
        DetectionCategory.allMethods.forEach { method ->
            insertDetection(packageName, userId, method.name, true)
        }
    }

    @Transaction
    suspend fun disableAllDetectionsForPackage(packageName: String, userId: Int) {
        DetectionCategory.allMethods.forEach { method ->
            insertDetection(packageName, userId, method.name, false)
        }
    }

    @Transaction
    suspend fun initializePackage(packageName: String, userId: Int, appId: Int) {
        insertPackageInfo(packageName, userId, appId)
        enableAllDetectionsForPackage(packageName, userId)
    }
}
