package top.ltfan.notdeveloper.ui.util

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import top.ltfan.material.m3.core.visual.LocalBackdrop
import top.ltfan.material.m3.core.visual.LocalBlurEnabled
import top.ltfan.material.m3.core.visual.backdropSurface

/**
 * Draws [containerColor] over the backdrop sampled by [LocalBackdrop], clipped to [shape]: a
 * vibrancy tint, and while [LocalBlurEnabled] is true a blur of [blurRadius]. The container color
 * is drawn at [surfaceAlpha] on top of the sampled backdrop.
 *
 * Without a backdrop host (for example in a preview) this falls back to a plain [containerColor]
 * background, so the layout stays visible.
 */
@Composable
fun Modifier.pageSurface(
    shape: Shape,
    containerColor: Color,
    blurRadius: Dp = 8.dp,
    surfaceAlpha: Float = 0.6f,
): Modifier {
    val backdrop = LocalBackdrop.current ?: return background(containerColor, shape)
    val blurEnabled = LocalBlurEnabled.current
    return backdropSurface(
        handle = backdrop,
        shape = shape,
        effects = {
            vibrancy()
            if (blurEnabled) blur(blurRadius.toPx())
        },
        onDrawSurface = { drawRect(containerColor.copy(alpha = surfaceAlpha)) },
    )
}
