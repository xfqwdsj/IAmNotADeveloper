package top.ltfan.notdeveloper.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class AutoRestorableState<T>(
    private val delegate: MutableState<T>,
    private val coroutineScope: CoroutineScope,
    private val delay: Duration = 1.seconds,
) : MutableState<T> by delegate {
    private val initialValue = delegate.value

    override var value: T
        get() = delegate.value
        set(newValue) {
            delegate.value = newValue
            coroutineScope.launch {
                delay(delay)
                delegate.value = initialValue
            }
        }

    constructor(
        initialValue: T,
        coroutineScope: CoroutineScope,
        delay: Duration = 1.seconds,
    ) : this(
        delegate = mutableStateOf(initialValue),
        delay = delay,
        coroutineScope = coroutineScope,
    )
}

@Composable
fun <T> rememberAutoRestorableState(
    initialValue: T,
    delay: Duration = 1.seconds,
): AutoRestorableState<T> {
    val coroutineScope = rememberCoroutineScope()
    return remember(initialValue, delay, coroutineScope) {
        AutoRestorableState(
            delegate = mutableStateOf(initialValue),
            delay = delay,
            coroutineScope = coroutineScope,
        )
    }
}
