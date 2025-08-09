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
import top.ltfan.notdeveloper.data.PackageInfo
import top.ltfan.notdeveloper.data.PackageSettingsDao
import top.ltfan.notdeveloper.data.ParcelablePackageInfo
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.util.doBroadcast

@BinderInterface
interface DatabaseServiceInterface {
    suspend fun insertPackageInfo(packageName: String, userId: Int, appId: Int)
    suspend fun deletePackageInfo(packageName: String, userId: Int)
    suspend fun getPackageInfoByName(packageName: String): List<ParcelablePackageInfo>
    fun listenPackageInfoByName(packageName: String, listener: PackageInfoListListener): Unlistener
    suspend fun getPackageInfoByUser(userId: Int): List<ParcelablePackageInfo>
    fun listenPackageInfoByUser(userId: Int, listener: PackageInfoListListener): Unlistener
    suspend fun isPackageExists(packageName: String, userId: Int): Boolean
    suspend fun isDetectionSet(packageName: String, userId: Int, methodName: String): Boolean
    suspend fun isDetectionEnabled(packageName: String, userId: Int, methodName: String): Boolean
    fun listenDetectionEnabled(
        packageName: String,
        userId: Int,
        methodName: String,
        listener: BooleanListener
    ): Unlistener

    suspend fun clearAllData()
    suspend fun toggleDetectionEnabled(packageName: String, userId: Int, methodName: String)
    suspend fun enableAllDetectionsForPackage(packageName: String, userId: Int)
    suspend fun disableAllDetectionsForPackage(packageName: String, userId: Int)
}

class DatabaseService(
    private val delegate: PackageSettingsDao,
) : DatabaseServiceInterface, PackageSettingsDao by delegate {
    private val packageInfoByNameListeners =
        mutableMapOf<String, Pair<Job, RemoteCallbackList<IPackageInfoListListener>>>()
    private val packageInfoByUserListeners =
        mutableMapOf<Int, Pair<Job, RemoteCallbackList<IPackageInfoListListener>>>()
    private val detectionEnabledListeners =
        mutableMapOf<Triple<String, Int, String>, Pair<Job, RemoteCallbackList<IBooleanListener>>>()

    override suspend fun getPackageInfoByName(packageName: String) =
        delegate.getPackageInfo(packageName).repack()

    override fun listenPackageInfoByName(
        packageName: String, listener: PackageInfoListListener
    ) = packageInfoByNameListeners.registerListener(
        key = packageName,
        listener = listener.toIListener(),
        flow = delegate::getPackageInfoFlow,
        broadcast = { this(it.repack()) },
    )

    override suspend fun getPackageInfoByUser(userId: Int) =
        delegate.getPackageInfo(userId).repack()

    override fun listenPackageInfoByUser(
        userId: Int, listener: PackageInfoListListener
    ) = packageInfoByUserListeners.registerListener(
        key = userId,
        listener = listener.toIListener(),
        flow = delegate::getPackageInfoFlow,
        broadcast = { this(it.repack()) },
    )

    override suspend fun isPackageExists(packageName: String, userId: Int) =
        delegate.isPackageExists(packageName, userId)

    override suspend fun isDetectionSet(
        packageName: String,
        userId: Int,
        methodName: String
    ) = delegate.isDetectionSet(packageName, userId, methodName)

    override suspend fun isDetectionEnabled(
        packageName: String,
        userId: Int,
        methodName: String
    ) = delegate.isDetectionEnabled(packageName, userId, methodName)

    override fun listenDetectionEnabled(
        packageName: String, userId: Int, methodName: String, listener: BooleanListener
    ) = detectionEnabledListeners.registerListener(
        key = Triple(packageName, userId, methodName),
        listener = listener.toIListener(),
        flow = { delegate.isDetectionEnabledFlow(it.first, it.second, it.third) },
        broadcast = { this(it) },
    )

    override suspend fun clearAllData() = delegate.clearAllData()

    override suspend fun toggleDetectionEnabled(
        packageName: String, userId: Int, methodName: String
    ) = delegate.toggleDetectionEnabled(packageName, userId, methodName)

    override suspend fun enableAllDetectionsForPackage(packageName: String, userId: Int) =
        delegate.enableAllDetectionsForPackage(packageName, userId)

    override suspend fun disableAllDetectionsForPackage(packageName: String, userId: Int) =
        delegate.disableAllDetectionsForPackage(packageName, userId)

    private inline fun <K, V, L : IListener> MutableMap<K, Pair<Job, RemoteCallbackList<L>>>.registerListener(
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

interface DatabaseServiceClient : DatabaseServiceInterface {
    suspend fun isDetectionSet(packageName: String, userId: Int, method: DetectionMethod) =
        isDetectionSet(packageName, userId, method.name)

    suspend fun isDetectionSet(packageName: String, userId: Int, methods: List<DetectionMethod>) =
        methods.map { isDetectionSet(packageName, userId, it) }

    suspend fun isDetectionEnabled(packageName: String, userId: Int, method: DetectionMethod) =
        isDetectionEnabled(packageName, userId, method.name)

    suspend fun isDetectionEnabled(
        packageName: String,
        userId: Int,
        methods: List<DetectionMethod>
    ) =
        methods.map { isDetectionEnabled(packageName, userId, it) }

    fun listenDetectionEnabled(
        packageName: String, userId: Int, method: DetectionMethod, listener: BooleanListener
    ): Unlistener = listenDetectionEnabled(packageName, userId, method.name, listener)

    fun listenDetectionEnabled(
        packageName: String, userId: Int, methods: List<DetectionMethod>, listener: BooleanListener
    ): Unlistener {
        val unlisteners = methods.map { listenDetectionEnabled(packageName, userId, it, listener) }
        return Unlistener {
            unlisteners.forEach { it() }
        }
    }

    suspend fun toggleDetectionEnabled(
        packageName: String, userId: Int, method: DetectionMethod
    ) = toggleDetectionEnabled(packageName, userId, method.name)

    companion object : DatabaseServiceClient {
        override suspend fun insertPackageInfo(packageName: String, userId: Int, appId: Int) {}
        override suspend fun deletePackageInfo(packageName: String, userId: Int) {}
        override suspend fun getPackageInfoByName(packageName: String) =
            emptyList<ParcelablePackageInfo>()

        override fun listenPackageInfoByName(
            packageName: String, listener: PackageInfoListListener
        ) = Unlistener

        override suspend fun getPackageInfoByUser(userId: Int) = emptyList<ParcelablePackageInfo>()
        override fun listenPackageInfoByUser(
            userId: Int, listener: PackageInfoListListener
        ) = Unlistener

        override suspend fun isPackageExists(packageName: String, userId: Int) = false
        override suspend fun isDetectionSet(
            packageName: String, userId: Int, methodName: String
        ) = false

        override suspend fun isDetectionEnabled(
            packageName: String, userId: Int, methodName: String
        ) = true

        override fun listenDetectionEnabled(
            packageName: String, userId: Int, methodName: String, listener: BooleanListener
        ) = Unlistener

        override suspend fun clearAllData() {}
        override suspend fun toggleDetectionEnabled(
            packageName: String, userId: Int, methodName: String
        ) {
        }

        override suspend fun enableAllDetectionsForPackage(
            packageName: String, userId: Int
        ) {
        }

        override suspend fun disableAllDetectionsForPackage(
            packageName: String, userId: Int
        ) {}
    }
}

fun DatabaseServiceClient(service: DatabaseServiceInterface): DatabaseServiceClient =
    object : DatabaseServiceClient, DatabaseServiceInterface by service {}

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
interface Listener

private interface IListener : IInterface, Listener

@BinderInterface
fun interface PackageInfoListListener : Listener {
    operator fun invoke(packageInfoList: List<ParcelablePackageInfo>)
}

private class IPackageInfoListListener(listener: PackageInfoListListener) : IListener,
    PackageInfoListListener by listener {
    override fun asBinder() = wrap()
}

private fun PackageInfoListListener.toIListener() = IPackageInfoListListener(this)

@BinderInterface
fun interface BooleanListener : Listener {
    operator fun invoke(enabled: Boolean)
}

private class IBooleanListener(listener: BooleanListener) : IListener, BooleanListener by listener {
    override fun asBinder(): IBinder = wrap()
}

private fun BooleanListener.toIListener() = IBooleanListener(this)
