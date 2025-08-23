package top.ltfan.notdeveloper.ui.composable

import androidx.annotation.StringRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconButtonWithTooltip(
    imageVector: ImageVector,
    @StringRes contentDescription: Int?,
    modifier: Modifier = Modifier,
    preferredTooltipPosition: TooltipPosition = TooltipPosition.Bottom,
    tooltipSpacing: Dp = 8.dp,
    onClick: () -> Unit,
) {
    val tooltipSpacing = with(LocalDensity.current) { tooltipSpacing.roundToPx() }
    TooltipBox(
        positionProvider = remember(preferredTooltipPosition, tooltipSpacing) {
            object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset {
                    val x = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2

                    val yTop = anchorBounds.top - popupContentSize.height - tooltipSpacing
                    val yBottom = anchorBounds.bottom + tooltipSpacing

                    val y = when (preferredTooltipPosition) {
                        TooltipPosition.Top -> yTop
                        TooltipPosition.Bottom -> yBottom
                    }.takeIf { it >= 0 && it <= windowSize.height - popupContentSize.height }
                        ?: when (preferredTooltipPosition) {
                            TooltipPosition.Top -> yBottom
                            TooltipPosition.Bottom -> yTop
                        }
                    return IntOffset(x, y)
                }
            }
        },
        tooltip = {
            contentDescription?.let { PlainTooltip { Text(stringResource(it)) } }
        },
        state = rememberTooltipState(),
        modifier = modifier,
        focusable = contentDescription != null,
        enableUserInput = contentDescription != null,
    ) {
        IconButton(
            onClick = onClick,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription?.let { stringResource(it) },
            )
        }
    }
}

enum class TooltipPosition {
    Top, Bottom
}
