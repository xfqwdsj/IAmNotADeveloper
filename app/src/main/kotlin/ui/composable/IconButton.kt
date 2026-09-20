package top.ltfan.notdeveloper.ui.composable

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.ltfan.material.m3.overlay.GlassPlainTooltip
import top.ltfan.material.m3.overlay.GlassTooltipAnchorPosition
import top.ltfan.material.m3.overlay.GlassTooltipBox
import top.ltfan.material.m3.overlay.rememberGlassTooltipPositionProvider
import top.ltfan.material.m3.overlay.rememberGlassTooltipState

@Composable
fun IconButtonWithTooltip(
    @DrawableRes iconRes: Int,
    @StringRes contentDescription: Int?,
    modifier: Modifier = Modifier,
    preferredTooltipPosition: GlassTooltipAnchorPosition = GlassTooltipAnchorPosition.Below,
    tooltipSpacing: Dp = 8.dp,
    onClick: () -> Unit,
) {
    val tooltipText = contentDescription?.let { stringResource(it) }
    val tooltipState = rememberGlassTooltipState()
    val positionProvider =
        rememberGlassTooltipPositionProvider(
            positioning = preferredTooltipPosition,
            spacingBetweenTooltipAndAnchor = tooltipSpacing,
        )

    Box(modifier) {
        GlassTooltipBox(
            tooltip = {
                GlassPlainTooltip {
                    Text(tooltipText.orEmpty())
                }
            },
            state = tooltipState,
            positionProvider = positionProvider,
            focusable = tooltipText != null,
            enableUserInput = tooltipText != null,
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = tooltipText,
                )
            }
        }
    }
}
