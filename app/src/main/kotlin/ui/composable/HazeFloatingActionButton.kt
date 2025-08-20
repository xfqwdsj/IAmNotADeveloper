package top.ltfan.notdeveloper.ui.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.LocalHazeStyle
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.ui.page.Page
import top.ltfan.notdeveloper.ui.util.FocusRequestingEffect
import top.ltfan.notdeveloper.ui.util.contentOverlayHaze
import top.ltfan.notdeveloper.ui.util.keepSizeWhenLookingAhead
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@Composable
context(viewModel: AppViewModel, page: Page)
fun HazeFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Button },
        shape = shape,
        color = Color.Transparent,
        contentColor = contentColor,
        interactionSource = interactionSource,
    ) {
        ProvideContentColorTextStyle(
            contentColor = contentColor,
            textStyle = MaterialTheme.typography.labelLarge,
        ) {
            Box(
                modifier =
                    Modifier
                        .defaultMinSize(
                            minWidth = 56.dp,
                            minHeight = 56.dp,
                        )
                        .contentOverlayHaze(
                            style = LocalHazeStyle.current.copy(
                                backgroundColor = containerColor,
                                tints = listOf(HazeDefaults.tint(containerColor)),
                            )
                        ),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
context(viewModel: AppViewModel, page: Page)
fun HazeFloatingActionButtonWithMenu(
    showMenu: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    interactionSource: MutableInteractionSource? = null,
    buttonContent: @Composable () -> Unit = {
        AnimatedContent(
            targetState = showMenu,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
        ) { showMenu ->
            val rotation by transition.animateFloat(
                label = "FabIconRotation",
                transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
            ) {
                val degrees = if (showMenu) -45f else 45f
                if (it == EnterExitState.Visible) 0f else degrees
            }

            val scale by transition.animateFloat(
                label = "FabIconScale",
                transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
            ) {
                val factor = if (showMenu) 1 / 1.27f else 1.27f
                if (it == EnterExitState.Visible) 1f else factor
            }

            if (showMenu) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.action_fab_menu_collapse),
                    modifier = Modifier
                        .scale(scale)
                        .rotate(rotation),
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_fab_menu_expand),
                    modifier = Modifier
                        .scale(scale)
                        .rotate(rotation),
                )
            }
        }
    },
    menuContent: @Composable ColumnScope.() -> Unit,
) {
    var size by remember { mutableStateOf(DpSize.Zero) }

    val focusRequester = remember { FocusRequester() }

    Box(contentAlignment = Alignment.BottomEnd) {
        SharedTransitionLayout {
            AnimatedContent(
                targetState = showMenu,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) { showMenu ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
                    horizontalAlignment = Alignment.End,
                ) {
                    if (showMenu) {
                        Box(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .verticalScroll(rememberScrollState())
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(
                                        FabSharedKey.Container
                                    ),
                                    animatedVisibilityScope = this@AnimatedContent,
                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                )
                                .clip(shape)
                                .contentOverlayHaze(
                                    style = LocalHazeStyle.current.copy(
                                        backgroundColor = containerColor,
                                        tints = listOf(HazeDefaults.tint(containerColor)),
                                    ),
                                    zIndexDelta = -.5f,
                                ),
                        ) {
                            val description = stringResource(R.string.description_fab_menu)
                            Column(
                                modifier = Modifier
                                    .keepSizeWhenLookingAhead()
                                    .focusRequester(focusRequester)
                                    .focusable()
                                    .semantics {
                                        contentDescription = description
                                    },
                                content = menuContent,
                            )

                            FocusRequestingEffect(focusRequester)
                        }
                    }

                    if (!showMenu) {
                        Spacer(
                            Modifier
                                .size(size)
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(
                                        FabSharedKey.Container
                                    ),
                                    animatedVisibilityScope = this@AnimatedContent,
                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                )
                                .clip(shape)
                                .contentOverlayHaze(
                                    style = LocalHazeStyle.current.copy(
                                        backgroundColor = containerColor,
                                        tints = listOf(HazeDefaults.tint(containerColor)),
                                    ),
                                    zIndexDelta = -.5f,
                                ),
                        )
                    } else {
                        Spacer(Modifier.size(size))
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Spacer(
                Modifier
                    .size(size)
                    .clip(shape)
                    .contentOverlayHaze(),
            )
        }

        val density = LocalDensity.current

        FloatingActionButton(
            onClick = onClick,
            modifier = modifier.onGloballyPositioned {
                with(density) {
                    size = DpSize(
                        width = it.size.width.toDp(),
                        height = it.size.height.toDp(),
                    )
                }
            },
            containerColor = Color.Transparent,
            contentColor = contentColor,
            elevation = FloatingActionButtonDefaults.elevation(
                0.dp, 0.dp, 0.dp, 0.dp,
            ),
            interactionSource = interactionSource,
            content = buttonContent,
        )
    }
}

enum class FabSharedKey { Container }
