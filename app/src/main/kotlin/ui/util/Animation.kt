package top.ltfan.notdeveloper.ui.util

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith

val AnimatedContentDefaultTransform =
    fadeIn(tween(220, 90)) + scaleIn(tween(220, 90), 0.92f) togetherWith fadeOut(tween(90))
