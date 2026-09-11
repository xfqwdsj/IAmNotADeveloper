package top.ltfan.notdeveloper.ui.util

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.runtimeShaderEffect
import com.kyant.backdrop.effects.vibrancy

/**
 * The edges a progressive blur can ramp from.
 */
enum class BackdropEdge { Top, Bottom, Start, End }

/**
 * Backdrop captured by the enclosing page; overlay surfaces sample it to
 * draw their glass.
 */
val LocalPageBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

/**
 * Chains a blurred backdrop whose strength ramps from full at [edge] to zero
 * at the opposite side. The ramp is a smoothstep mask applied to the blurred
 * content, so the clear end stays sharp; devices that cannot run the mask
 * shader fall back to a uniform [blur] of the same [radius].
 */
fun BackdropEffectScope.progressiveBlur(
    radius: Float,
    edge: BackdropEdge,
    curve: Float = 1f,
) {
    blur(radius)
    runtimeShaderEffect("ProgressiveBlur", ProgressiveBlurShader, "content") {
        setFloatUniform("size", size.width, size.height)
        setFloatUniform(
            "axis",
            if (edge == BackdropEdge.Top || edge == BackdropEdge.Bottom) 1f else 0f,
        )
        setFloatUniform(
            "flip",
            if (edge == BackdropEdge.Bottom || edge == BackdropEdge.End) 1f else 0f,
        )
        setFloatUniform("curve", curve)
    }
}

private const val ProgressiveBlurShader = """
uniform shader content;

uniform float2 size;
uniform float axis;
uniform float flip;
uniform float curve;

half4 main(float2 coord) {
    float2 uv = coord / size;
    float t = mix(uv.x, uv.y, axis);
    t = mix(t, 1.0 - t, flip);
    float ramp = pow(clamp(t, 0.0, 1.0), max(curve, 0.001));
    float alpha = 1.0 - smoothstep(0.0, 1.0, ramp);
    return content.eval(coord) * alpha;
}
"""

@Composable
fun rememberPageBackdrop(background: Color): LayerBackdrop =
    rememberLayerBackdrop {
        drawRect(background)
        drawContent()
    }

fun Modifier.pageBackdrop(backdrop: LayerBackdrop): Modifier = layerBackdrop(backdrop)

fun Modifier.glassSurface(
    backdrop: Backdrop,
    shape: Shape,
    effects: BackdropEffectScope.() -> Unit,
    onDrawSurface: (DrawScope.() -> Unit)? = null,
): Modifier = drawBackdrop(
    backdrop = backdrop,
    shape = { shape },
    effects = effects,
    onDrawSurface = onDrawSurface,
)

/**
 * Draws a glass surface for the enclosing page: it samples
 * [LocalPageBackdrop] when present and falls back to a flat [containerColor]
 * fill otherwise.
 */
@Composable
fun Modifier.pageGlass(
    shape: Shape,
    containerColor: Color,
    blurRadius: Dp = 8.dp,
    surfaceAlpha: Float = 0.6f,
): Modifier {
    val backdrop = LocalPageBackdrop.current
        ?: return background(containerColor)
    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            vibrancy()
            blur(blurRadius.toPx())
        },
        onDrawSurface = { drawRect(containerColor.copy(alpha = surfaceAlpha)) },
    )
}
