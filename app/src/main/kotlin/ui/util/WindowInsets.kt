package top.ltfan.notdeveloper.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp
import top.ltfan.material.m3.core.layout.plus

/** Height reserved by the floating bottom bar. */
val LocalBottomBarHeight = compositionLocalOf { 0.dp }

/** The window insets extended by the floating bottom bar height. */
val AppWindowInsets
    @Composable inline get() =
        WindowInsets.safeDrawing + PaddingValues(bottom = LocalBottomBarHeight.current)
