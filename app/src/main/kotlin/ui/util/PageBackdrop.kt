package top.ltfan.notdeveloper.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.ltfan.m3x.backdrop.BackdropHost
import top.ltfan.m3x.backdrop.BackdropLayerHandle
import top.ltfan.m3x.backdrop.captureBackdrop
import top.ltfan.m3x.backdrop.rememberBackdropLayer

/**
 * Captures page content for sibling chrome. Materials inside the capture sample only the separate
 * background layer, so they never read the layer that is currently recording them.
 */
@Composable
fun PageBackdropContent(pageLayer: BackdropLayerHandle, content: @Composable () -> Unit) {
    val background = MaterialTheme.colorScheme.background
    val backgroundLayer = rememberBackdropLayer(background)
    Box(Modifier.fillMaxSize().captureBackdrop(pageLayer)) {
        Box(Modifier.matchParentSize().captureBackdrop(backgroundLayer).background(background))
        BackdropHost(backgroundLayer, content)
    }
}
