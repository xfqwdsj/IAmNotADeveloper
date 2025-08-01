package top.ltfan.notdeveloper.ui.util

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import top.ltfan.dslutilities.LockableValueDsl

@PaddingValuesOperationScope.Dsl
class PaddingValuesOperationScope(
    val first: PaddingValues,
    val second: PaddingValues
) : LockableValueDsl() {
    var start by required<Dp>()
    var top by required<Dp>()
    var end by required<Dp>()
    var bottom by required<Dp>()

    @SuppressLint("ComposableNaming")
    @Composable
    inline fun start(block: DpBuilder.() -> Dp) {
        start = DpBuilder(first.start, second.start).block()
    }

    inline fun top(block: DpBuilder.() -> Dp) {
        top = DpBuilder(first.top, second.top).block()
    }

    @SuppressLint("ComposableNaming")
    @Composable
    inline fun end(block: DpBuilder.() -> Dp) {
        end = DpBuilder(first.end, second.end).block()
    }

    inline fun bottom(block: DpBuilder.() -> Dp) {
        bottom = DpBuilder(first.bottom, second.bottom).block()
    }

    @Dsl
    class DpBuilder(val first: Dp, val second: Dp) {
        val plus inline get() = first + second
        val minus inline get() = first - second
    }

    @Composable
    fun build(): PaddingValues {
        lock()
        return PaddingValues(start, top, end, bottom)
    }

    val PaddingValues.start: Dp
        @Composable inline get() = calculateStartPadding(LocalLayoutDirection.current)

    val PaddingValues.top inline get() = calculateTopPadding()

    val PaddingValues.end: Dp
        @Composable inline get() = calculateEndPadding(LocalLayoutDirection.current)

    val PaddingValues.bottom inline get() = calculateBottomPadding()

    @DslMarker
    annotation class Dsl
}

@Composable
operator fun PaddingValues.plus(other: PaddingValues) = (this with other) {
    start { plus }
    top { plus }
    end { plus }
    bottom { plus }
}

@Composable
operator fun PaddingValues.minus(other: PaddingValues) = (this with other) {
    start { minus }
    top { minus }
    end { minus }
    bottom { minus }
}

infix fun PaddingValues.with(other: PaddingValues): @Composable (@Composable PaddingValuesOperationScope.() -> Unit) -> PaddingValues =
    {
        PaddingValuesOperationScope(this, other).apply { it() }.build()
    }
