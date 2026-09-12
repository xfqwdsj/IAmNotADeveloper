package top.ltfan.notdeveloper.ui.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntRect
import top.ltfan.material.m3.core.visual.pageGlass
import top.ltfan.material.m3.overlay.LocalOverlayHost
import top.ltfan.material.m3.overlay.OverlayEntry

@Composable
fun IconButtonWithTooltip(
    imageVector: ImageVector,
    @StringRes contentDescription: Int?,
    modifier: Modifier = Modifier,
    preferredTooltipPosition: TooltipPosition = TooltipPosition.Bottom,
    tooltipSpacing: Dp = 8.dp,
    onClick: () -> Unit,
) {
    val host = LocalOverlayHost.current
    val spacing = with(LocalDensity.current) { tooltipSpacing.roundToPx() }
    var anchor by remember { mutableStateOf(IntRect.Zero) }
    var shown by remember { mutableStateOf(false) }
    val entry = remember { OverlayEntry() }

    if (host != null && contentDescription != null) {
        entry.content = {
            if (shown) {
                InWindowTooltip(
                    anchor = anchor,
                    position = preferredTooltipPosition,
                    spacing = spacing,
                    text = stringResource(contentDescription),
                )
            }
        }
        DisposableEffect(host, contentDescription) {
            host.add(entry)
            onDispose {
                host.remove(entry)
                shown = false
            }
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { anchor = it.boundsInWindow().roundToIntRect() }
            .pointerInput(host, contentDescription) {
                if (host == null || contentDescription == null) return@pointerInput
                detectTapGestures(
                    onLongPress = { shown = true },
                    onPress = {
                        tryAwaitRelease()
                        shown = false
                    },
                )
            },
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription?.let { stringResource(it) },
            )
        }
    }
}

@Composable
private fun InWindowTooltip(
    anchor: IntRect,
    position: TooltipPosition,
    spacing: Int,
    text: String,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val x = anchor.left + (anchor.width - size.width) / 2
    val y = when (position) {
        TooltipPosition.Top -> anchor.top - size.height - spacing
        TooltipPosition.Bottom -> anchor.bottom + spacing
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(x, y) }
            .onSizeChanged { size = it }
            .pageGlass(
                shape = MaterialTheme.shapes.small,
                containerColor = MaterialTheme.colorScheme.surface,
                blurRadius = 4.dp,
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

enum class TooltipPosition {
    Top, Bottom
}
