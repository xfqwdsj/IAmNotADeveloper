package top.ltfan.notdeveloper.ui.composables

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import kotlin.reflect.KProperty

@Composable
fun rememberBooleanSharedPreference(
    preferenceFileKey: String? = null,
    mode: Int = Context.MODE_PRIVATE,
    key: String,
    defaultValue: Boolean
): BooleanSharedPreference {
    val context = LocalContext.current
    val preference = remember(key) {
        BooleanSharedPreference(
            context, preferenceFileKey, mode, key, defaultValue
        )
    }

    DisposableEffect(preference) {
        onDispose {
            preference.clear()
        }
    }

    return preference
}

class BooleanSharedPreference(
    context: Context,
    preferenceFileKey: String? = null,
    mode: Int = Context.MODE_PRIVATE,
    private val key: String,
    private val defaultValue: Boolean
) {
    private val sharedPreferences = kotlin.runCatching {
        context.getSharedPreferences(
            preferenceFileKey ?: (context.packageName + "_preferences"), mode
        )
    }.getOrNull()

    private val listener = OnSharedPreferenceChangeListener { sharedPreferences, changedKey ->
        if (changedKey != key) {
            return@OnSharedPreferenceChangeListener
        }

        value = sharedPreferences.getBoolean(key, defaultValue)
    }

    init {
        sharedPreferences?.registerOnSharedPreferenceChangeListener(listener)
    }

    private val prefsValue get() = sharedPreferences?.getBoolean(key, defaultValue) ?: defaultValue

    private var value by mutableStateOf(prefsValue)

    operator fun getValue(thisObj: Any?, property: KProperty<*>) = value

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: Boolean) {
        sharedPreferences?.edit { putBoolean(key, value) }
        this.value = prefsValue
    }

    fun clear() {
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
