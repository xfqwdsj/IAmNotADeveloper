package top.ltfan.notdeveloper.service

import android.os.IBinder
import android.os.IInterface
import android.os.RemoteCallbackList
import com.github.kr328.kaidl.BinderInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import top.ltfan.notdeveloper.data.PackageInfo
import top.ltfan.notdeveloper.data.PackageSettingsDao
import top.ltfan.notdeveloper.data.ParcelablePackageInfo
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.util.doBroadcast
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.minutes

@BinderInterface
interface DatabaseServiceInterface {
    fun insertPackageInfo(packageName: String, userId: Int, appId: Int, isSuccess: BooleanDatabaseCallback)
    fun deletePackageInfo(packageName: String, userId: Int, isSuccess: BooleanDatabaseCallback)
    fun getPackageInfoByName(packageName: String, get: PackageInfoListDatabaseCallback)
    fun listenPackageInfoByName(packageName: String, listener: PackageInfoListDatabaseCallback): Unlistener
    fun getPackageInfoByUser(userId: Int, get: PackageInfoListDatabaseCallback)
    fun listenPackageInfoByUser(userId: Int, listener: PackageInfoListDatabaseCallback): Unlistener
    fun isPackageExists(packageName: String, userId: Int, get: BooleanDatabaseCallback)
    fun isDetectionSet(packageName: String, userId: Int, methodName: String, get: BooleanDatabaseCallback)
    fun isDetectionEnabled(packageName: String, userId: Int, methodName: String, get: BooleanDatabaseCallback)
    fun listenDetectionEnabled(packageName: String, userId: Int, methodName: String, listener: BooleanDatabaseCallback): Unlistener
    fun clearAllData(isSuccess: BooleanDatabaseCallback)
    fun toggleDetectionEnabled(packageName: String, userId: Int, methodName: String, isSuccess: BooleanDatabaseCallback)
    fun enableAllDetectionsForPackage(packageName: String, userId: Int, isSuccess: BooleanDatabaseCallback)
    fun disableAllDetectionsForPackage(packageName: String, userId: Int, isSuccess: BooleanDatabaseCallback)
}

class DatabaseService(
    private val delegate: PackageSettingsDao,
) : DatabaseServiceInterface {
    private val packageInfoByNameListeners =
        mutableMapOf<String, Pair<Job, RemoteCallbackList<IPackageInfoListDatabaseCallback>>>()
    private val packageInfoByUserListeners =
        mutableMapOf<Int, Pair<Job, RemoteCallbackList<IPackageInfoListDatabaseCallback>>>()
    private val detectionEnabledListeners =
        mutableMapOf<Triple<String, Int, String>, Pair<Job, RemoteCallbackList<IBooleanDatabaseCallback>>>()

    override fun insertPackageInfo(
        packageName: String, userId: Int, appId: Int, isSuccess: BooleanDatabaseCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            delegate.insertPackageInfo(packageName, userId, appId)
        }.invokeOnCompletion { cause ->
            if (cause == null) {
                isSuccess(true)
                return@invokeOnCompletion
            }

            Log.w("Failed to insert package info for $packageName", cause)
            isSuccess(false)
        }
    }

    override fun deletePackageInfo(
        packageName: String, userId: Int, isSuccess: BooleanDatabaseCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            delegate.deletePackageInfo(packageName, userId)
        }.invokeOnCompletion { cause ->
            if (cause == null) {
                isSuccess(true)
                return@invokeOnCompletion
            }

            Log.w("Failed to delete package info for $packageName", cause)
            isSuccess(false)
        }
    }

    override fun getPackageInfoByName(packageName: String, get: PackageInfoListDatabaseCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            get(delegate.getPackageInfo(packageName).repack())
        }
    }

    override fun listenPackageInfoByName(
        packageName: String, listener: PackageInfoListDatabaseCallback
    ) = packageInfoByNameListeners.registerListener(
        key = packageName,
        listener = listener.toICallback(),
        flow = delegate::getPackageInfoFlow,
        broadcast = { this(it.repack()) },
    )

    override fun getPackageInfoByUser(userId: Int, get: PackageInfoListDatabaseCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            get(delegate.getPackageInfo(userId).repack())
        }
    }

    override fun listenPackageInfoByUser(
        userId: Int, listener: PackageInfoListDatabaseCallback
    ) = packageInfoByUserListeners.registerListener(
        key = userId,
        listener = listener.toICallback(),
        flow = delegate::getPackageInfoFlow,
        broadcast = { this(it.repack()) },
    )

    override fun isPackageExists(packageName: String, userId: Int, get: BooleanDatabaseCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            get(delegate.isPackageExists(packageName, userId))
        }
    }

    override fun isDetectionSet(
        packageName: String, userId: Int, methodName: String, get: BooleanDatabaseCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            get(delegate.isDetectionSet(packageName, userId, methodName))
        }
    }

    override fun isDetectionEnabled(
        packageName: String, userId: Int, methodName: String, get: BooleanDatabaseCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            get(delegate.isDetectionEnabled(packageName, userId, methodName))
        }
    }

    override fun listenDetectionEnabled(
        packageName: String, userId: Int, methodName: String, listener: BooleanDatabaseCallback
    ) = detectionEnabledListeners.registerListener(
        key = Triple(packageName, userId, methodName),
        listener = listener.toICallback(),
        flow = { delegate.isDetectionEnabledFlow(it.first, it.second, it.third) },
        broadcast = { this(it) },
    )

    override fun clearAllData(isSuccess: BooleanDatabaseCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            delegate.clearAllData()
        }.invokeOnCompletion { cause ->
            if (cause == null) {
                isSuccess(true)
                return@invokeOnCompletion
            }

            isSuccess(false)
            Log.w("Failed to clear all data", cause)
        }
    }

    override fun toggleDetectionEnabled(
        packageName: String, userId: Int, methodName: String, isSuccess: BooleanDatabaseCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            delegate.toggleDetectionEnabled(packageName, userId, methodName)
        }.invokeOnCompletion { cause ->
            if (cause == null) {
                isSuccess(true)
                return@invokeOnCompletion
            }

            Log.w("Failed to toggle detection enabled", cause)
            isSuccess(false)
        }
    }

    override fun enableAllDetectionsForPackage(
        packageName: String, userId: Int, isSuccess: BooleanDatabaseCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            delegate.enableAllDetectionsForPackage(packageName, userId)
        }.invokeOnCompletion { cause ->
            if (cause == null) {
                isSuccess(true)
                return@invokeOnCompletion
            }

            Log.w("Failed to enable all detections for package $packageName", cause)
            isSuccess(false)
        }
    }

    override fun disableAllDetectionsForPackage(
        packageName: String, userId: Int, isSuccess: BooleanDatabaseCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            delegate.disableAllDetectionsForPackage(packageName, userId)
        }.invokeOnCompletion { cause ->
            if (cause == null) {
                isSuccess(true)
                return@invokeOnCompletion
            }

            Log.w("Failed to disable all detections for package $packageName", cause)
            isSuccess(false)
        }
    }

    private inline fun <K, V, L : IDatabaseCallback> MutableMap<K, Pair<Job, RemoteCallbackList<L>>>.registerListener(
        key: K,
        listener: L,
        crossinline flow: (K) -> Flow<V>,
        crossinline broadcast: L.(V) -> Unit,
    ): Unlistener {
        val unlistener = Unlistener {
            val map = this
            synchronized(map) {
                val list = map[key]?.second
                list?.unregister(listener)
                if (list?.registeredCallbackCount != 0) return@Unlistener
                map.remove(key)
            }?.let { (job, list) ->
                job.cancel()
                list.kill()
            }
        }

        this[key]?.second?.register(listener)?.also {
            return unlistener
        }

        val list = RemoteCallbackList<L>()
        val job = CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            flow(key).collect { value ->
                list.doBroadcast { it.broadcast(value) }
            }
        }
        val pairToAdd = job to list

        var outdated = false
        val pair = synchronized(this) {
            val existingPair = this[key]
            if (existingPair != null) {
                outdated = true
                existingPair
            } else {
                this[key] = pairToAdd
                pairToAdd
            }
        }

        if (outdated) {
            pairToAdd.first.cancel()
            pairToAdd.second.kill()
        }

        pair.second.register(listener)
        return unlistener
    }

    private fun List<PackageInfo>.repack() = map { ParcelablePackageInfo(it) }
}

val DatabaseServiceInterface.client inline get() = DatabaseServiceClient(this)

interface DatabaseServiceClient {
    suspend fun insertPackageInfo(packageName: String, userId: Int, appId: Int)
    suspend fun deletePackageInfo(packageName: String, userId: Int)
    suspend fun getPackageInfo(packageName: String): List<PackageInfo>
    fun listenPackageInfo(packageName: String, listener: (List<PackageInfo>) -> Unit): Unlistener
    suspend fun getPackageInfo(userId: Int): List<PackageInfo>
    fun listenPackageInfo(userId: Int, listener: (List<PackageInfo>) -> Unit): Unlistener
    suspend fun isPackageExists(packageName: String, userId: Int): Boolean
    suspend fun isDetectionSet(packageName: String, userId: Int, methodName: String): Boolean
    suspend fun isDetectionSet(packageName: String, userId: Int, method: DetectionMethod) =
        isDetectionSet(packageName, userId, method.name)
    suspend fun isDetectionSet(packageName: String, userId: Int, methods: List<DetectionMethod>) =
        methods.map { isDetectionSet(packageName, userId, it) }
    suspend fun isDetectionEnabled(packageName: String, userId: Int, methodName: String): Boolean
    suspend fun isDetectionEnabled(packageName: String, userId: Int, method: DetectionMethod) =
        isDetectionEnabled(packageName, userId, method.name)
    suspend fun isDetectionEnabled(packageName: String, userId: Int, methods: List<DetectionMethod>) =
        methods.map { isDetectionEnabled(packageName, userId, it) }
    fun listenDetectionEnabled(packageName: String, userId: Int, methodName: String, listener: BooleanDatabaseCallback): Unlistener
    fun listenDetectionEnabled(packageName: String, userId: Int, method: DetectionMethod, listener: BooleanDatabaseCallback) =
        listenDetectionEnabled(packageName, userId, method.name, listener)
    fun listenDetectionEnabled(packageName: String, userId: Int, methods: List<DetectionMethod>, listener: BooleanDatabaseCallback) =
        methods.map { listenDetectionEnabled(packageName, userId, it, listener) }
    suspend fun clearAllData()
    suspend fun toggleDetectionEnabled(packageName: String, userId: Int, methodName: String)
    suspend fun toggleDetectionEnabled(packageName: String, userId: Int, method: DetectionMethod) =
        toggleDetectionEnabled(packageName, userId, method.name)
    suspend fun toggleDetectionEnabled(packageName: String, userId: Int, methods: List<DetectionMethod>) =
        methods.forEach { toggleDetectionEnabled(packageName, userId, it) }
    suspend fun enableAllDetectionsForPackage(packageName: String, userId: Int)
    suspend fun disableAllDetectionsForPackage(packageName: String, userId: Int)

    companion object : DatabaseServiceClient {
        override suspend fun insertPackageInfo(packageName: String, userId: Int, appId: Int) {}
        override suspend fun deletePackageInfo(packageName: String, userId: Int) {}
        override suspend fun getPackageInfo(packageName: String) = emptyList<PackageInfo>()
        override fun listenPackageInfo(
            packageName: String, listener: (List<PackageInfo>) -> Unit
        ) = Unlistener
        override suspend fun getPackageInfo(userId: Int) = emptyList<PackageInfo>()
        override fun listenPackageInfo(
            userId: Int, listener: (List<PackageInfo>) -> Unit
        ) = Unlistener
        override suspend fun isPackageExists(packageName: String, userId: Int) = false
        override suspend fun isDetectionSet(
            packageName: String, userId: Int, methodName: String
        ) = false
        override suspend fun isDetectionEnabled(
            packageName: String, userId: Int, methodName: String
        ) = true
        override fun listenDetectionEnabled(
            packageName: String, userId: Int, methodName: String, listener: BooleanDatabaseCallback
        ) = Unlistener
        override suspend fun clearAllData() {}
        override suspend fun toggleDetectionEnabled(
            packageName: String, userId: Int, methodName: String
        ) {}
        override suspend fun enableAllDetectionsForPackage(
            packageName: String, userId: Int
        ) {}
        override suspend fun disableAllDetectionsForPackage(
            packageName: String, userId: Int
        ) {}
    }
}

fun DatabaseServiceClient(service: DatabaseServiceInterface): DatabaseServiceClient =
    object : DatabaseServiceClient, DatabaseServiceInterface by service {
        override suspend fun insertPackageInfo(packageName: String, userId: Int, appId: Int) {
            withTimeout(10.minutes) {
                suspendCoroutine { continuation ->
                    insertPackageInfo(packageName, userId, appId) {
                        continuation.resume(Unit)
                    }
                }
            }
        }

        override suspend fun deletePackageInfo(packageName: String, userId: Int) {
            withTimeout(10.minutes) {
                suspendCoroutine { continuation ->
                    deletePackageInfo(packageName, userId) {
                        continuation.resume(Unit)
                    }
                }
            }
        }

        override suspend fun getPackageInfo(packageName: String): List<PackageInfo> {
            return withTimeout(10.minutes) {
                suspendCoroutine { continuation ->
                    getPackageInfoByName(packageName) { packageInfoList ->
                        continuation.resume(packageInfoList.restore())
                    }
                }
            }
        }

        override fun listenPackageInfo(
            packageName: String, listener: (List<PackageInfo>) -> Unit
        ) = listenPackageInfoByName(packageName) {
            listener(it.restore())
        }

        override suspend fun getPackageInfo(userId: Int): List<PackageInfo> {
            return withTimeout(10.minutes) {
                suspendCoroutine { continuation ->
                    getPackageInfoByUser(userId) { packageInfoList ->
                        continuation.resume(packageInfoList.restore())
                    }
                }
            }
        }

        override fun listenPackageInfo(
            userId: Int, listener: (List<PackageInfo>) -> Unit
        ) = listenPackageInfoByUser(userId) {
            listener(it.restore())
        }

        override suspend fun isPackageExists(packageName: String, userId: Int): Boolean {
            return withTimeout(10.minutes) {
                suspendCoroutine { continuation ->
                    isPackageExists(packageName, userId) { exists ->
                        continuation.resume(exists)
                    }
                }
            }
        }

        override suspend fun isDetectionSet(
            packageName: String, userId: Int, methodName: String
        ): Boolean {
            return withTimeout(10.minutes) {
                suspendCoroutine { continuation ->
                    isDetectionSet(packageName, userId, methodName) { isSet ->
                        continuation.resume(isSet)
                    }
                }
            }
        }

        override suspend fun isDetectionEnabled(
            packageName: String, userId: Int, methodName: String
        ): Boolean {
            return withTimeout(10.minutes) {
                suspendCoroutine { continuation ->
                    isDetectionEnabled(packageName, userId, methodName) { isEnabled ->
                        continuation.resume(isEnabled)
                    }
                }
            }
        }

        override suspend fun clearAllData() {
            withTimeout(10.minutes) {
                suspendCoroutine { continuation ->
                    clearAllData {
                        continuation.resume(Unit)
                    }
                }
            }
        }

        override suspend fun toggleDetectionEnabled(
            packageName: String, userId: Int, methodName: String
        ) {
            withTimeout(10.minutes) {
                suspendCoroutine { continuation ->
                    toggleDetectionEnabled(packageName, userId, methodName) {
                        continuation.resume(Unit)
                    }
                }
            }
        }

        override suspend fun enableAllDetectionsForPackage(packageName: String, userId: Int) {
            withTimeout(10.minutes) {
                suspendCoroutine { continuation ->
                    enableAllDetectionsForPackage(packageName, userId) {
                        continuation.resume(Unit)
                    }
                }
            }
        }

        override suspend fun disableAllDetectionsForPackage(packageName: String, userId: Int) {
            withTimeout(10.minutes) {
                suspendCoroutine { continuation ->
                    disableAllDetectionsForPackage(packageName, userId) {
                        continuation.resume(Unit)
                    }
                }
            }
        }

        private fun List<ParcelablePackageInfo>.restore() = map { it.restore() }
    }

@BinderInterface
fun interface Unlistener {
    operator fun invoke()

    companion object : Unlistener {
        override fun invoke() {}
    }
}

inline fun Unlistener(crossinline block: () -> Unit): Unlistener = object : Unlistener {
    override fun invoke() = block()
}

@BinderInterface
interface DatabaseCallback

private interface IDatabaseCallback : IInterface, DatabaseCallback

@BinderInterface
fun interface PackageInfoListDatabaseCallback : DatabaseCallback {
    operator fun invoke(packageInfoList: List<ParcelablePackageInfo>)
}

private class IPackageInfoListDatabaseCallback(listener: PackageInfoListDatabaseCallback) : IDatabaseCallback,
    PackageInfoListDatabaseCallback by listener {
    override fun asBinder() = wrap()
}

private fun PackageInfoListDatabaseCallback.toICallback() = IPackageInfoListDatabaseCallback(this)

@BinderInterface
fun interface BooleanDatabaseCallback : DatabaseCallback {
    operator fun invoke(value: Boolean)
}

private class IBooleanDatabaseCallback(listener: BooleanDatabaseCallback) : IDatabaseCallback, BooleanDatabaseCallback by listener {
    override fun asBinder(): IBinder = wrap()
}

private fun BooleanDatabaseCallback.toICallback() = IBooleanDatabaseCallback(this)
