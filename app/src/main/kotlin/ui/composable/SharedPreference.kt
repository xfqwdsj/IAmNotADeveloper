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
            preference.clean()
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
) {
    private val listener = OnSharedPreferenceChangeListener { _, changedKey ->
        if (changedKey == key) {
            value = prefsValue
        }
    }

    init {
        preferences?.registerOnSharedPreferenceChangeListener(listener)
    }

    private val prefsValue get() = preferences?.getBoolean(key, defaultValue) ?: defaultValue

    private var value by mutableStateOf(prefsValue)

    operator fun getValue(thisObj: Any?, property: KProperty<*>) = value

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefsValue = beforeSet?.invoke(value) ?: value
                val editor = preferences?.edit()
                if (editor != null) {
                    editor.putBoolean(key, prefsValue)
                    // The write goes through the framework service and can fail.
                    if (!editor.commit()) {
                        Log.Android.e("failed to save $key to the framework")
                    }
                }
                this@BooleanSharedPreference.value = prefsValue
                afterSet?.invoke(prefsValue)
            } catch (e: Exception) {
                Log.Android.e("failed to save $key to the framework", e)
            }
        }
    }

    fun clean() {
        preferences?.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
