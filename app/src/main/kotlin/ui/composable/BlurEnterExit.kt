package top.ltfan.notdeveloper.ui.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedVisibilityScope.BlurEnterExit(
    modifier: Modifier = Modifier, maxRadius: Dp = 8.dp, content: @Composable BoxScope.() -> Unit
) {
    val blurRadius by transition.animateDp { if (it != EnterExitState.Visible) maxRadius else 0.dp }
    Box(modifier.blur(blurRadius), content = content)
}

@Composable
context(transition: Transition<Boolean>)
fun ColumnScope.AnimatedVisibilityWithBlur(
    modifier: Modifier = Modifier,
    maxRadius: Dp = 8.dp,
    enter: EnterTransition = fadeIn() + expandVertically(clip = false),
    exit: ExitTransition = fadeOut() + shrinkVertically(clip = false),
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    transition.AnimatedVisibility(
        visible = { it },
        enter = enter,
        exit = exit,
        modifier = modifier,
    ) {
        BlurEnterExit(
            maxRadius = maxRadius,
        ) {
            content()
        }
    }
}

@Composable
fun ColumnScope.AnimatedVisibilityWithBlur(
    visible: Boolean,
    modifier: Modifier = Modifier,
    maxRadius: Dp = 8.dp,
    enter: EnterTransition = fadeIn() + expandVertically(clip = false),
    exit: ExitTransition = fadeOut() + shrinkVertically(clip = false),
    label: String = "AnimatedVisibility",
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = enter,
        exit = exit,
        modifier = modifier,
        label = label,
    ) {
        BlurEnterExit(
            maxRadius = maxRadius,
        ) {
            content()
        }
    }
}
