package top.ltfan.notdeveloper.ui.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import top.ltfan.backdrop.BackdropEffectScope
import top.ltfan.backdrop.BackdropMask
import top.ltfan.backdrop.BackdropRamp
import top.ltfan.backdrop.effects.blur
import top.ltfan.backdrop.isRuntimeShaderSupported
import top.ltfan.m3x.compose.ui.visual.BackdropBlurEnabled
import top.ltfan.m3x.material.backdrop.theme.M3xBackdropTheme
import top.ltfan.m3x.material.backdrop.theme.backdrop
import top.ltfan.m3x.material.theme.extended

@Composable
fun appBarEffects(progressive: Boolean): (BackdropEffectScope.() -> Unit)? {
    if (useProgressiveAppBar(progressive)) return null
    val enabled = BackdropBlurEnabled
    val radius = MaterialTheme.extended.backdrop.scrollEdge.blurRadius
    return { if (enabled) blur(radius.toPx()) }
}

private fun useProgressiveAppBar(progressive: Boolean): Boolean =
    progressive && isRuntimeShaderSupported()

fun appBarMask(progressive: Boolean): BackdropMask? =
    if (useProgressiveAppBar(progressive)) BackdropMask() else null

fun appBarBackdropTheme(progressive: Boolean): M3xBackdropTheme {
    val defaults = M3xBackdropTheme()
    if (useProgressiveAppBar(progressive)) return defaults
    // The material renderer reserves the ramp's coverage independently of its effects and mask.
    // A surface-bounded ramp keeps uniform blur inside the bar while retaining sampling outsets.
    return defaults.copy(scrollEdge = defaults.scrollEdge.copy(ramp = BackdropRamp()))
}
