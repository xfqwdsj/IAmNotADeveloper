package top.ltfan.notdeveloper.ui.composable

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import top.ltfan.notdeveloper.xposed.Log
import kotlin.reflect.KProperty

/**
 * Wraps the boolean stored under [key] in [preferences] as Compose state.
 *
 * [preferences] is the remote preferences of
 * [top.ltfan.notdeveloper.ModuleService], which only exist once the
 * framework has connected, so it may be `null` for a while; the holder
 * is then recreated as soon as the preferences become available.
 */
@Composable
fun rememberBooleanSharedPreference(
    preferences: SharedPreferences?,
    key: String,
    defaultValue: Boolean,
    beforeSet: ((Boolean) -> Boolean)? = null,
    afterSet: ((Boolean) -> Unit)? = null,
): BooleanSharedPreference {
    val preference = remember(preferences, key) {
        BooleanSharedPreference(preferences, key, defaultValue, beforeSet, afterSet)
    }

    DisposableEffect(preference) {
        onDispose {
            preference.close()
        }
    }

    return preference
}

class BooleanSharedPreference(
    private val preferences: SharedPreferences?,
    private val key: String,
    private val defaultValue: Boolean,
    private val beforeSet: ((Boolean) -> Boolean)? = null,
    private val afterSet: ((Boolean) -> Unit)? = null,
) : AutoCloseable {
    private val listener = OnSharedPreferenceChangeListener { _, changedKey ->
        if (changedKey == key) {
            value = prefsValue
        }
    }

    /**
     * Pending writes for [key]; while one write runs, a newer request replaces
     * the buffered one, so only the latest survives.
     */
    private val requests = Channel<Boolean>(Channel.CONFLATED)
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        preferences?.registerOnSharedPreferenceChangeListener(listener)
        scope.launch {
            for (requested in requests) {
                persist(requested)
            }
        }
    }

    private val prefsValue get() = preferences?.getBoolean(key, defaultValue) ?: defaultValue

    private var value by mutableStateOf(prefsValue)

    operator fun getValue(thisObj: Any?, property: KProperty<*>) = value

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: Boolean) {
        requests.trySend(value)
    }

    private fun persist(value: Boolean) {
        try {
            val newValue = beforeSet?.invoke(value) ?: value
            val previousValue = prefsValue
            val editor = preferences?.edit()
            if (editor != null) {
                editor.putBoolean(key, newValue)
                // The framework write follows the local update, so a failed
                // commit leaves the displayed value ahead of the stored one.
                if (!editor.commit()) {
                    Log.Android.e("failed to save $key to the framework")
                    this@BooleanSharedPreference.value = previousValue
                    return
                }
            }
            this@BooleanSharedPreference.value = newValue
            afterSet?.invoke(newValue)
        } catch (e: Exception) {
            Log.Android.e("failed to save $key to the framework", e)
        }
    }

    override fun close() {
        requests.close()
        scope.cancel()
        preferences?.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
