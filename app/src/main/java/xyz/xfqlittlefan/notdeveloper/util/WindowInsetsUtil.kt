package xyz.xfqlittlefan.notdeveloper.util

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable

operator fun WindowInsets.plus(other: WindowInsets): WindowInsets = add(other)

val WindowInsets.Companion.allBars: WindowInsets
    @Composable get() = WindowInsets.systemBars + WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)