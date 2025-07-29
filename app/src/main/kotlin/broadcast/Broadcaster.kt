package top.ltfan.notdeveloper.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.xposed.Log
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val BroadcastActionChange = "notdeveloper.action.Change"
const val BroadcastActionChangeCallback = "notdeveloper.action.Change.Callback"
const val BroadcastExtraId = "notdeveloper.extra.Id"
const val BroadcastExtraMethod = "notdeveloper.extra.Method"

private val receivers = mutableMapOf<String, Pair<() -> Unit, BroadcastReceiver>>()

@OptIn(ExperimentalUuidApi::class)
fun Context.broadcastChange(method: DetectionMethod, callback: () -> Unit) {
    val id = Uuid.random().toString()
    registerCallbackReceiver(id, callback)
    val intent = Intent(BroadcastActionChange)
    intent.putExtra(BroadcastExtraId, id)
    intent.putExtra(BroadcastExtraMethod, method::class.java.name)
    intent.setPackage("android")
    sendBroadcast(intent)
}

fun Context.receiveChangeBroadcast(receiver: (DetectionMethod) -> Unit): () -> Unit {
    val intentFilter = IntentFilter(BroadcastActionChange)

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val methodName = intent.getStringExtra(BroadcastExtraMethod)
            val id = intent.getStringExtra(BroadcastExtraId)

            if (methodName != null) {
                try {
                    val methodClass = Class.forName(methodName).kotlin
                    val method = methodClass.objectInstance as? DetectionMethod
                        ?: error("Invalid method class: $methodName")
                    receiver(method)

                    val intent = Intent(BroadcastActionChangeCallback)
                    intent.putExtra(BroadcastExtraId, id)
                    intent.setPackage(BuildConfig.APPLICATION_ID)
                    sendBroadcast(intent)
                } catch (e: Throwable) {
                    Log.e("Error receiving broadcast change for method: $methodName", e)
                    cleanup(id)
                }
            }
        }
    }

    ContextCompat.registerReceiver(
        this,
        broadcastReceiver,
        intentFilter,
        ContextCompat.RECEIVER_EXPORTED
    )

    return { unregisterReceiver(broadcastReceiver) }
}

private fun Context.registerCallbackReceiver(id: String, callback: () -> Unit) {
    val intentFilter = IntentFilter(BroadcastActionChangeCallback)

    val job = CoroutineScope(Dispatchers.IO).launch {
        delay(30.seconds)
        callback()
        cleanup(id)
    }

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getStringExtra(BroadcastExtraId)
            if (id == null) {
                Log.Android.w("Received callback without ID")
                return
            }
            val receiverPair = receivers.remove(id)
            if (receiverPair == null) {
                Log.Android.w("Received callback for unknown ID: $id")
                return
            }
            receiverPair.first.invoke()
            job.cancel()
            unregisterReceiver(receiverPair.second)
        }
    }

    ContextCompat.registerReceiver(
        this,
        broadcastReceiver,
        intentFilter,
        ContextCompat.RECEIVER_EXPORTED
    )

    receivers[id] = Pair(callback, broadcastReceiver)
}

private fun Context.cleanup(id: String?) {
    val receiverPair = receivers.remove(id)
    if (receiverPair == null) {
        Log.Android.d("No receiver found for ID: $id")
        return
    }
    try {
        unregisterReceiver(receiverPair.second)
    } catch (e: IllegalArgumentException) {
        Log.Android.d("Failed to unregister receiver for ID: $id", e)
    }
}
