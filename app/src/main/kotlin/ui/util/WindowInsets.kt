package top.ltfan.notdeveloper.ui.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

val LocalBottomBarHeight = compositionLocalOf { 0.dp }

val AppWindowInsets
    @Composable inline get() =
        WindowInsets.safeDrawing + PaddingValues(bottom = LocalBottomBarHeight.current)

@WindowInsetsSidesBuilder.Dsl
object WindowInsetsSidesBuilder {
    val start = WindowInsetsSides.Start
    val top = WindowInsetsSides.Top
    val end = WindowInsetsSides.End
    val bottom = WindowInsetsSides.Bottom
    val vertical = WindowInsetsSides.Vertical
    val horizontal = WindowInsetsSides.Horizontal

    @DslMarker
    annotation class Dsl
}

inline fun WindowInsets.only(block: WindowInsetsSidesBuilder.() -> WindowInsetsSides) =
    this.only(WindowInsetsSidesBuilder.block())

@Stable
@Composable
operator fun WindowInsets.plus(padding: PaddingValues): WindowInsets {
    val layoutDirection = LocalLayoutDirection.current

    val left = remember(layoutDirection) { padding.calculateLeftPadding(layoutDirection) }
    val top = remember { padding.calculateTopPadding() }
    val right = remember(layoutDirection) { padding.calculateRightPadding(layoutDirection) }
    val bottom = remember { padding.calculateBottomPadding() }

    return remember(this, left, top, right, bottom) {
        ScaledWindowInsets(
            base = this,
            leftAdd = left,
            topAdd = top,
            rightAdd = right,
            bottomAdd = bottom,
        )
    }
}

@Immutable
private data class ScaledWindowInsets(
    private val base: WindowInsets,
    private val leftAdd: Dp = 0.dp,
    private val topAdd: Dp = 0.dp,
    private val rightAdd: Dp = 0.dp,
    private val bottomAdd: Dp = 0.dp,
) : WindowInsets {
    override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int =
        base.getLeft(density, layoutDirection) + with(density) { leftAdd.roundToPx() }

    override fun getTop(density: Density): Int =
        base.getTop(density) + with(density) { topAdd.roundToPx() }

    override fun getRight(density: Density, layoutDirection: LayoutDirection): Int =
        base.getRight(density, layoutDirection) + with(density) { rightAdd.roundToPx() }

    override fun getBottom(density: Density): Int =
        base.getBottom(density) + with(density) { bottomAdd.roundToPx() }
}

@Composable
fun WindowInsetsToPaddingValuesBox(
    insets: WindowInsets,
    content: @Composable (paddingValues: PaddingValues) -> Unit,
) {
    var consumedInsets by remember { mutableStateOf(WindowInsets()) }
    Box(Modifier.onConsumedWindowInsetsChanged { consumedInsets = it }) {
        content(insets.exclude(consumedInsets).asPaddingValues())
    }
}
