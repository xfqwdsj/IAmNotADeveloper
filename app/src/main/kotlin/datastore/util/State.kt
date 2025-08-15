package top.ltfan.notdeveloper.datastore.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import top.ltfan.notdeveloper.datastore.model.AppDataStore

@Composable
fun <T, R> AppDataStore<T>.rememberPropertyAsState(
    defaultValue: T = this.defaultValue,
    get: (T) -> R,
    set: (T, R) -> T,
): MutableState<R> {
    val coroutineScope = rememberCoroutineScope()
    val delegate = rememberSaveable { mutableStateOf(defaultValue) }

    LaunchedEffect(defaultValue, delegate) {
        data.collect { delegate.value = it }
    }

    return remember(get, set, coroutineScope, delegate) {
        object : MutableState<R> by mutableStateOf(get(delegate.value)) {
            override var value: R
                get() = get(delegate.value)
                set(newValue) {
                    coroutineScope.launch {
                        updateData { set(delegate.value, newValue) }
                    }
                }
        }
    }
}
