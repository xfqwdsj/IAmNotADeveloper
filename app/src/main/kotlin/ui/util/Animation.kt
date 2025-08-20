package top.ltfan.notdeveloper.ui.util

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester

val EmptyContentTransform = ContentTransform(
    targetContentEnter = EnterTransition.None,
    initialContentExit = ExitTransition.None,
    sizeTransform = null,
)

val AnimatedContentDefaultTransform =
    fadeIn(tween(220, 90)) + scaleIn(tween(220, 90), 0.92f) togetherWith fadeOut(tween(90))

@Composable
fun AnimatedVisibilityScope.FocusRequestingEffect(focusRequester: FocusRequester) {
    var requested by remember { mutableStateOf(false) }

    if (!requested) {
        LaunchedEffect(transition.currentState) {
            if (transition.currentState == EnterExitState.Visible) {
                focusRequester.requestFocus()
            }
        }
    }
}
