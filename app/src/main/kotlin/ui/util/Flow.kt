package top.ltfan.notdeveloper.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlin.properties.ReadWriteProperty
import kotlinx.coroutines.flow.Flow

@Composable
fun <T : R, R> Flow<T>.collectAsMutableState(
    initial: R,
    set: (R) -> Unit,
): ReadWriteProperty<Any?, R> {
    val state = remember(initial) { mutableStateOf(initial) }
    val set by rememberUpdatedState(set)

    LaunchedEffect(this, state) {
        collect { state.value = it }
    }

    return object : ReadWriteProperty<Any?, R> {
        override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): R = state.value

        override fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: R) {
            set(value)
        }
    }
}
