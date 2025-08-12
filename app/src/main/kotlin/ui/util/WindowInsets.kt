package top.ltfan.notdeveloper.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import top.ltfan.dslutilities.LockableValueDsl

val AppWindowInsets @Composable inline get() = WindowInsets.safeDrawing

@WindowInsetsSidesBuilder.Dsl
class WindowInsetsSidesBuilder {
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
    this.only(WindowInsetsSidesBuilder().block())

@WindowInsetsOperationScope.Dsl
class WindowInsetsOperationScope(private val insets: WindowInsets) : LockableValueDsl() {
    var left by prepared<(inset: Dp) -> Dp>({ it })
    var top by prepared<(inset: Dp) -> Dp>({ it })
    var right by prepared<(inset: Dp) -> Dp>({ it })
    var bottom by prepared<(inset: Dp) -> Dp>({ it })

    fun left(block: (inset: Dp) -> Dp) {
        left = block
    }

    fun top(block: (inset: Dp) -> Dp) {
        top = block
    }

    fun right(block: (inset: Dp) -> Dp) {
        right = block
    }

    fun bottom(block: (inset: Dp) -> Dp) {
        bottom = block
    }

    fun asWindowInsets(): WindowInsets {
        lock()
        return object : WindowInsets {
            override fun getLeft(density: Density, layoutDirection: LayoutDirection) =
                with(density) {
                    left.invoke(insets.getLeft(density, layoutDirection).toDp()).roundToPx()
                }

            override fun getTop(density: Density) =
                with(density) {
                    top.invoke(insets.getTop(density).toDp()).roundToPx()
                }

            override fun getRight(density: Density, layoutDirection: LayoutDirection) =
                with(density) {
                    right.invoke(insets.getRight(density, layoutDirection).toDp()).roundToPx()
                }

            override fun getBottom(density: Density) =
                with(density) {
                    bottom.invoke(insets.getBottom(density).toDp()).roundToPx()
                }
        }
    }

    @Composable
    fun asPaddingValues(): PaddingValues {
        lock()
        return PaddingValues.build {
            val density = LocalDensity.current
            val layoutDirection = LocalLayoutDirection.current

            val getLeft = this@WindowInsetsOperationScope.left
            val getTop = this@WindowInsetsOperationScope.top
            val getRight = this@WindowInsetsOperationScope.right
            val getBottom = this@WindowInsetsOperationScope.bottom
            val insets = this@WindowInsetsOperationScope.insets

            with(density) {
                start = when (layoutDirection) {
                    LayoutDirection.Ltr -> getLeft(
                        insets.getLeft(density, layoutDirection).toDp()
                    )

                    LayoutDirection.Rtl -> getRight(
                        insets.getRight(density, layoutDirection).toDp()
                    )
                }

                top = getTop(insets.getTop(density).toDp())

                end = when (layoutDirection) {
                    LayoutDirection.Ltr -> getRight(
                        insets.getRight(density, layoutDirection).toDp()
                    )

                    LayoutDirection.Rtl -> getLeft(
                        insets.getLeft(density, layoutDirection).toDp()
                    )
                }

                bottom = getBottom(insets.getBottom(density).toDp())
            }
        }
    }

    @DslMarker
    annotation class Dsl
}

@Composable
inline fun WindowInsets.operateAsWindowInsets(
    block: @Composable WindowInsetsOperationScope.() -> Unit
) = WindowInsetsOperationScope(this).apply {
    block()
}.asWindowInsets()

@Composable
operator fun WindowInsets.plus(padding: PaddingValues) = operateAsWindowInsets {
    val paddingLeft = padding.left
    val paddingTop = padding.top
    val paddingRight = padding.right
    val paddingBottom = padding.bottom

    left { it + paddingLeft }
    top { it + paddingTop }
    right { it + paddingRight }
    bottom { it + paddingBottom }
}
