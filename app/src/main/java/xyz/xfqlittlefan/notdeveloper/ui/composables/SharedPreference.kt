package xyz.xfqlittlefan.notdeveloper.ui.composables

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import kotlin.reflect.KProperty

@Composable
fun rememberBooleanSharedPreference(key: String, defaultValue: Boolean): BooleanSharedPreference {
    val context = LocalContext.current
    val preference = remember(key) { BooleanSharedPreference(context, key, defaultValue) }

    DisposableEffect(preference) {
        onDispose {
            preference.clear()
        }
    }

    return preference
}

class BooleanSharedPreference(context: Context, private val key: String, private val defaultValue: Boolean) {
    private val sharedPreferences = getDefaultSharedPreferences(context)

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