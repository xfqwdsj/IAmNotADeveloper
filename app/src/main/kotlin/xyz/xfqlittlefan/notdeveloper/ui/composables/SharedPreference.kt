package xyz.xfqlittlefan.notdeveloper.ui.composables

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlin.reflect.KProperty

@Composable
fun rememberBooleanSharedPreference(
    preferenceFileKey: String? = null,
    mode: Int = Context.MODE_PRIVATE,
    key: String,
    defaultValue: Boolean
): BooleanSharedPreference {
    val context = LocalContext.current
    val preference =
        remember(key) {
            BooleanSharedPreference(
                context,
                preferenceFileKey,
                mode,
                key,
                defaultValue
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
    private val sharedPreferences = context.getSharedPreferences(
        preferenceFileKey ?: (context.packageName + "_preferences"), mode
    )

    private val listener = OnSharedPreferenceChangeListener { sharedPreferences, changedKey ->
        if (changedKey != key) {
            return@OnSharedPreferenceChangeListener
        }

        value = sharedPreferences.getBoolean(key, defaultValue)
    }

    private var value by mutableStateOf(sharedPreferences.getBoolean(key, defaultValue))

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    operator fun getValue(thisObj: Any?, property: KProperty<*>) = value

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
        this.value = value
    }

    fun clear() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}