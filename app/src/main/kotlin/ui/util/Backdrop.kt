package top.ltfan.notdeveloper.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.runtimeShaderEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

/**
 * The edges a progressive blur can ramp from.
 */
enum class BackdropEdge { Top, Bottom, Start, End }

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
fun rememberPageBackdrop(background: Color): com.kyant.backdrop.backdrops.LayerBackdrop =
    rememberLayerBackdrop {
        drawRect(background)
        drawContent()
    }

fun Modifier.pageBackdrop(backdrop: com.kyant.backdrop.backdrops.LayerBackdrop): Modifier =
    layerBackdrop(backdrop)

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
