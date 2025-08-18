package top.ltfan.notdeveloper.ui.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.ltfan.notdeveloper.ui.util.AnimatedContentDefaultTransform

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
    label: String = "AnimatedVisibilityWithBlur",
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibilityWithBlurImpl(
        visible = visible,
        modifier = modifier,
        maxRadius = maxRadius,
        enter = enter,
        exit = exit,
        label = label,
        content = content,
    )
}

@Composable
context(transition: Transition<Boolean>)
fun RowScope.AnimatedVisibilityWithBlur(
    modifier: Modifier = Modifier,
    maxRadius: Dp = 8.dp,
    enter: EnterTransition = fadeIn() + expandHorizontally(clip = false),
    exit: ExitTransition = fadeOut() + shrinkHorizontally(clip = false),
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
fun RowScope.AnimatedVisibilityWithBlur(
    visible: Boolean,
    modifier: Modifier = Modifier,
    maxRadius: Dp = 8.dp,
    enter: EnterTransition = fadeIn() + expandHorizontally(clip = false),
    exit: ExitTransition = fadeOut() + shrinkHorizontally(clip = false),
    label: String = "AnimatedVisibilityWithBlur",
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibilityWithBlurImpl(
        visible = visible,
        modifier = modifier,
        maxRadius = maxRadius,
        enter = enter,
        exit = exit,
        label = label,
        content = content,
    )
}

@Composable
fun AnimatedVisibilityWithBlur(
    visible: Boolean,
    modifier: Modifier = Modifier,
    maxRadius: Dp = 8.dp,
    enter: EnterTransition = fadeIn() + expandIn(clip = false),
    exit: ExitTransition = fadeOut() + shrinkOut(clip = false),
    label: String = "AnimatedVisibilityWithBlur",
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibilityWithBlurImpl(
        visible = visible,
        modifier = modifier,
        maxRadius = maxRadius,
        enter = enter,
        exit = exit,
        label = label,
        content = content,
    )
}

@Composable
fun AnimatedVisibilityWithBlur(
    visible: Boolean,
    direction: EnterExitPredefinedDirection,
    modifier: Modifier = Modifier,
    maxRadius: Dp = 8.dp,
    label: String = "AnimatedVisibilityWithBlur",
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibilityWithBlurImpl(
        visible = visible,
        modifier = modifier,
        maxRadius = maxRadius,
        enter = direction.enter,
        exit = direction.exit,
        label = label,
        content = content,
    )
}

@Composable
fun AnimatedVisibilityWithBlurImpl(
    visible: Boolean,
    modifier: Modifier = Modifier,
    maxRadius: Dp = 8.dp,
    enter: EnterTransition,
    exit: ExitTransition,
    label: String = "AnimatedVisibilityWithBlurImpl",
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

enum class EnterExitPredefinedDirection(
    val enter: EnterTransition,
    val exit: ExitTransition,
) {
    Vertical(
        enter = fadeIn() + expandVertically(clip = false),
        exit = fadeOut() + shrinkVertically(clip = false),
    ),
    Horizontal(
        enter = fadeIn() + expandHorizontally(clip = false),
        exit = fadeOut() + shrinkHorizontally(clip = false),
    )
}

@Composable
fun <S> AnimatedContentWithBlur(
    targetState: S,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        AnimatedContentDefaultTransform
    },
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "AnimatedContentWithBlur",
    contentKey: (targetState: S) -> Any? = { it },
    content: @Composable() AnimatedContentScope.(targetState: S) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = transitionSpec,
        contentAlignment = contentAlignment,
        label = label,
        contentKey = contentKey,
    ) { targetState ->
        BlurEnterExit(
            maxRadius = 8.dp,
        ) {
            content(targetState)
        }
    }
}
