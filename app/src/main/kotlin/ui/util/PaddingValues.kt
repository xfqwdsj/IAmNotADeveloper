package top.ltfan.notdeveloper.ui.util

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import top.ltfan.dslutilities.LockableValueDsl

@TwoPaddingValuesOperationScope.Dsl
class TwoPaddingValuesOperationScope(
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

    @DslMarker
    annotation class Dsl
}

@PaddingValuesOperationScope.Dsl
class PaddingValuesOperationScope(private val padding: PaddingValues) : LockableValueDsl() {
    var start by required<Dp>()
    var top by required<Dp>()
    var end by required<Dp>()
    var bottom by required<Dp>()

    @SuppressLint("ComposableNaming")
    @Composable
    fun init() {
        start = padding.start
        top = padding.top
        end = padding.end
        bottom = padding.bottom
    }

    @Composable
    fun build(): PaddingValues {
        lock()
        return PaddingValues(start, top, end, bottom)
    }

    @DslMarker
    annotation class Dsl
}

val PaddingValues.left: Dp
    @Composable inline get() = calculateLeftPadding(LocalLayoutDirection.current)

val PaddingValues.start: Dp
    @Composable inline get() = calculateStartPadding(LocalLayoutDirection.current)

val PaddingValues.top inline get() = calculateTopPadding()

val PaddingValues.right: Dp
    @Composable inline get() = calculateRightPadding(LocalLayoutDirection.current)

val PaddingValues.end: Dp
    @Composable inline get() = calculateEndPadding(LocalLayoutDirection.current)

val PaddingValues.bottom inline get() = calculateBottomPadding()

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

infix fun PaddingValues.with(other: PaddingValues): @Composable (@Composable TwoPaddingValuesOperationScope.() -> Unit) -> PaddingValues =
    {
        TwoPaddingValuesOperationScope(this, other).apply { it() }.build()
    }

@Composable
inline fun PaddingValues.operate(block: @Composable PaddingValuesOperationScope.() -> Unit) =
    PaddingValuesOperationScope(this).apply {
        init()
        block()
    }.build()

@Composable
inline fun PaddingValues.Companion.build(block: @Composable PaddingValuesOperationScope.() -> Unit): PaddingValues =
    PaddingValuesOperationScope(PaddingValues.Zero).apply {
        init()
        block()
    }.build()

fun PaddingValues.asWindowInsets(): WindowInsets = PaddingValuesInsets(this)

class PaddingValuesInsets(private val paddingValues: PaddingValues) : WindowInsets {
    override fun getLeft(density: Density, layoutDirection: LayoutDirection) =
        with(density) { paddingValues.calculateLeftPadding(layoutDirection).roundToPx() }

    override fun getTop(density: Density) =
        with(density) { paddingValues.calculateTopPadding().roundToPx() }

    override fun getRight(density: Density, layoutDirection: LayoutDirection) =
        with(density) { paddingValues.calculateRightPadding(layoutDirection).roundToPx() }

    override fun getBottom(density: Density) =
        with(density) { paddingValues.calculateBottomPadding().roundToPx() }

    override fun toString(): String {
        val layoutDirection = LayoutDirection.Ltr
        val start = paddingValues.calculateLeftPadding(layoutDirection)
        val top = paddingValues.calculateTopPadding()
        val end = paddingValues.calculateRightPadding(layoutDirection)
        val bottom = paddingValues.calculateBottomPadding()
        return "PaddingValues($start, $top, $end, $bottom)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is PaddingValuesInsets) {
            return false
        }

        return other.paddingValues == paddingValues
    }

    override fun hashCode(): Int = paddingValues.hashCode()
}
