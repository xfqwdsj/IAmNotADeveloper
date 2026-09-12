package top.ltfan.notdeveloper.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

@Stable
fun PaddingValues.copy(
    layoutDirection: LayoutDirection,
    start: Dp = this.calculateStartPadding(layoutDirection),
    top: Dp = this.calculateTopPadding(),
    end: Dp = this.calculateEndPadding(layoutDirection),
    bottom: Dp = this.calculateBottomPadding(),
) = PaddingValues(
    start = start,
    top = top,
    end = end,
    bottom = bottom,
)

@Stable
@Composable
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    val start1 = this.calculateStartPadding(layoutDirection)
    val top1 = this.calculateTopPadding()
    val end1 = this.calculateEndPadding(layoutDirection)
    val bottom1 = this.calculateBottomPadding()
    val start2 = other.calculateStartPadding(layoutDirection)
    val top2 = other.calculateTopPadding()
    val end2 = other.calculateEndPadding(layoutDirection)
    val bottom2 = other.calculateBottomPadding()

    return remember(
        start1, top1, end1, bottom1, start2, top2, end2, bottom2,
    ) {
        PaddingValues(
            start = start1 + start2,
            top = top1 + top2,
            end = end1 + end2,
            bottom = bottom1 + bottom2,
        )
    }
}

@Stable
@Composable
operator fun PaddingValues.minus(other: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    val start1 = this.calculateStartPadding(layoutDirection)
    val top1 = this.calculateTopPadding()
    val end1 = this.calculateEndPadding(layoutDirection)
    val bottom1 = this.calculateBottomPadding()
    val start2 = other.calculateStartPadding(layoutDirection)
    val top2 = other.calculateTopPadding()
    val end2 = other.calculateEndPadding(layoutDirection)
    val bottom2 = other.calculateBottomPadding()

    return remember(
        start1, top1, end1, bottom1, start2, top2, end2, bottom2,
    ) {
        PaddingValues(
            start = start1 - start2,
            top = top1 - top2,
            end = end1 - end2,
            bottom = bottom1 - bottom2,
        )
    }
}

@Composable
fun PaddingValues.asWindowInsets(): WindowInsets = remember(this) { PaddingValuesInsets(this) }

@Stable
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
