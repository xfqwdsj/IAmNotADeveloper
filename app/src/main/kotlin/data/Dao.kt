package top.ltfan.notdeveloper.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import top.ltfan.notdeveloper.detection.DetectionCategory

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

    @Query("SELECT EXISTS(SELECT 1 FROM PackageInfo WHERE packageName = :packageName AND userId = :userId)")
    suspend fun isPackageExists(packageName: String, userId: Int): Boolean

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
    suspend fun clearAllData()

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
}
