package top.ltfan.notdeveloper.xposed

import android.os.IInterface
import android.os.RemoteCallbackList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import top.ltfan.notdeveloper.data.PackageInfo
import top.ltfan.notdeveloper.data.PackageSettingsDao
import top.ltfan.notdeveloper.data.ParcelablePackageInfo
import top.ltfan.notdeveloper.service.data.IBooleanListener
import top.ltfan.notdeveloper.service.data.IPackageInfoListListener
import top.ltfan.notdeveloper.service.data.IPackageSettingsDao
import top.ltfan.notdeveloper.service.data.IUnlistener
import top.ltfan.notdeveloper.util.doBroadcast

class PackageSettingsDaoService(private val delegate: PackageSettingsDao) :
    IPackageSettingsDao.Stub() {
    private val packageInfoByNameListeners =
        mutableMapOf<String, Pair<Job, RemoteCallbackList<IPackageInfoListListener>>>()
    private val packageInfoByUserListeners =
        mutableMapOf<Int, Pair<Job, RemoteCallbackList<IPackageInfoListListener>>>()
    private val detectionEnabledListeners =
        mutableMapOf<Triple<String, Int, String>, Pair<Job, RemoteCallbackList<IBooleanListener>>>()

    override fun insertPackageInfo(packageName: String, userId: Int, appId: Int) {
        runBlocking(Dispatchers.IO) {
            delegate.insertPackageInfo(packageName, userId, appId)
        }
    }

    override fun deletePackageInfo(packageName: String, userId: Int) {
        runBlocking(Dispatchers.IO) {
            delegate.deletePackageInfo(packageName, userId)
        }
    }

    override fun getPackageInfoByName(packageName: String): List<ParcelablePackageInfo> {
        return runBlocking(Dispatchers.IO) {
            delegate.getPackageInfo(packageName).repack()
        }
    }

    override fun listenPackageInfoByName(
        packageName: String, listener: IPackageInfoListListener
    ) = packageInfoByNameListeners.registerListener(
        key = packageName,
        listener = listener,
        flow = delegate::getPackageInfoFlow,
        broadcast = { this(it.repack()) },
    )

    override fun getPackageInfoByUser(userId: Int): List<ParcelablePackageInfo> {
        return runBlocking(Dispatchers.IO) {
            delegate.getPackageInfo(userId).repack()
        }
    }

    override fun listenPackageInfoByUser(
        userId: Int, listener: IPackageInfoListListener
    ) = packageInfoByUserListeners.registerListener(
        key = userId,
        listener = listener,
        flow = delegate::getPackageInfoFlow,
        broadcast = { this(it.repack()) },
    )

    override fun isPackageInfoExists(packageName: String, userId: Int): Boolean {
        return runBlocking(Dispatchers.IO) {
            delegate.isPackageExists(packageName, userId)
        }
    }

    override fun isDetectionEnabled(packageName: String, userId: Int, methodName: String): Boolean {
        return runBlocking(Dispatchers.IO) {
            delegate.isDetectionEnabled(packageName, userId, methodName)
        }
    }

    override fun listenDetectionEnabled(
        packageName: String, userId: Int, methodName: String, listener: IBooleanListener
    ) = detectionEnabledListeners.registerListener(
        key = Triple(packageName, userId, methodName),
        listener = listener,
        flow = { delegate.isDetectionEnabledFlow(it.first, it.second, it.third) },
        broadcast = { this(it) },
    )

    override fun clearAllData() {
        runBlocking(Dispatchers.IO) {
            delegate.clearAllData()
        }
    }

    override fun toggleDetectionEnabled(packageName: String, userId: Int, methodName: String) {
        runBlocking(Dispatchers.IO) {
            delegate.toggleDetectionEnabled(packageName, userId, methodName)
        }
    }

    override fun enableAllDetectionsForPackage(packageName: String, userId: Int) {
        runBlocking(Dispatchers.IO) {
            delegate.enableAllDetectionsForPackage(packageName, userId)
        }
    }

    override fun disableAllDetectionsForPackage(packageName: String, userId: Int) {
        runBlocking(Dispatchers.IO) {
            delegate.disableAllDetectionsForPackage(packageName, userId)
        }
    }

    private inline fun <K, V, Listener : IInterface> MutableMap<K, Pair<Job, RemoteCallbackList<Listener>>>.registerListener(
        key: K,
        listener: Listener,
        crossinline flow: (K) -> Flow<V>,
        crossinline broadcast: Listener.(V) -> Unit,
    ): IUnlistener {
        val unlistener = object : IUnlistener.Stub() {
            override fun invoke() {
                val map = this@registerListener
                synchronized(map) {
                    val list = map[key]?.second
                    list?.unregister(listener)
                    if (list?.registeredCallbackCount != 0) return
                    map.remove(key)
                }?.let { (job, list) ->
                    job.cancel()
                    list.kill()
                }
            }
        }

        this[key]?.second?.register(listener)?.also {
            return unlistener
        }

        val list = RemoteCallbackList<Listener>()
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
