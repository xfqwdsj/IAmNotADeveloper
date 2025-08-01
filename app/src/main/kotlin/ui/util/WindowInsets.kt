package top.ltfan.notdeveloper.ui.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable

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
