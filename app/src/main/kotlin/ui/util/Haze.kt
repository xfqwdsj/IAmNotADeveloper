package top.ltfan.notdeveloper.ui.util

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeEffectScope
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import top.ltfan.notdeveloper.ui.page.Page
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@OptIn(ExperimentalHazeMaterialsApi::class)
val HazeStyleAppBar @Composable inline get() = HazeMaterials.ultraThick()
val HazeEasing = CubicBezierEasing(.2f, .0f, .2f, 1f)

context(viewModel: AppViewModel)
fun Modifier.hazeSource(
    state: HazeState = viewModel.hazeState,
    zIndex: Float = 0f,
    key: Any? = null,
) = hazeSource(state, zIndex, key)

context(viewModel: AppViewModel, page: Page)
fun Modifier.pageHazeSource(
    state: HazeState = viewModel.hazeState,
    zIndex: Float = 0f,
) = hazeSource(state, zIndex, HazeKey(page, zIndex))

context(viewModel: AppViewModel)
fun Modifier.hazeEffect(
    state: HazeState = viewModel.hazeState,
    style: HazeStyle = HazeStyle.Unspecified,
    block: (HazeEffectScope.() -> Unit)? = null,
) = hazeEffect(state, style, block)

@Composable
context(viewModel: AppViewModel)
fun Modifier.hazeEffectStart(
    state: HazeState = viewModel.hazeState,
    style: HazeStyle = HazeStyleAppBar,
    easing: Easing = HazeEasing,
    block: (HazeEffectScope.() -> Unit)? = null,
) = hazeEffect(state, style) {
    progressive = HazeProgressive.horizontalGradient(
        easing = easing,
        startX = Float.POSITIVE_INFINITY,
        endX = 0f,
    )
    block?.invoke(this)
}

@Composable
context(viewModel: AppViewModel)
fun Modifier.hazeEffectTop(
    state: HazeState = viewModel.hazeState,
    style: HazeStyle = HazeStyleAppBar,
    easing: Easing = HazeEasing,
    block: (HazeEffectScope.() -> Unit)? = null,
) = hazeEffect(state, style) {
    progressive = HazeProgressive.verticalGradient(
        easing = easing,
        startY = Float.POSITIVE_INFINITY,
        endY = 0f,
    )
    block?.invoke(this)
}

@Composable
context(viewModel: AppViewModel)
fun Modifier.hazeEffectEnd(
    state: HazeState = viewModel.hazeState,
    style: HazeStyle = HazeStyleAppBar,
    easing: Easing = HazeEasing,
    block: (HazeEffectScope.() -> Unit)? = null,
) = hazeEffect(state, style) {
    progressive = HazeProgressive.horizontalGradient(easing = easing)
    block?.invoke(this)
}

@Composable
context(viewModel: AppViewModel)
fun Modifier.hazeEffectBottom(
    state: HazeState = viewModel.hazeState,
    style: HazeStyle = HazeStyleAppBar,
    easing: Easing = HazeEasing,
    block: (HazeEffectScope.() -> Unit)? = null,
) = hazeEffect(state, style) {
    progressive = HazeProgressive.verticalGradient(easing = easing)
    block?.invoke(this)
}

context(viewModel: AppViewModel, page: Page)
fun Modifier.contentHazeSource(
    state: HazeState = viewModel.hazeState,
    zIndexDelta: Float = 0f,
) = pageHazeSource(
    state = state,
    zIndex = HazeZIndex.content + zIndexDelta,
)

@Composable
context(viewModel: AppViewModel, page: Page)
fun Modifier.appBarHaze(
    state: HazeState = viewModel.hazeState,
    style: HazeStyle = HazeStyleAppBar,
    easing: Easing = HazeEasing,
    zIndexDelta: Float = 0f,
    block: (HazeEffectScope.() -> Unit)? = null,
) = this
    .pageHazeSource(
        state = state,
        zIndex = HazeZIndex.topBar + zIndexDelta,
    )
    .hazeEffectTop(state, style, easing) {
        drawPageArea(HazeZIndex.topBar + zIndexDelta)
        block?.invoke(this)
    }

context(viewModel: AppViewModel, page: Page)
fun Modifier.contentOverlayHaze(
    state: HazeState = viewModel.hazeState,
    style: HazeStyle = HazeStyle.Unspecified,
    zIndexDelta: Float = 0f,
    block: (HazeEffectScope.() -> Unit)? = null,
) = this
    .pageHazeSource(
        state = state,
        zIndex = HazeZIndex.contentOverlay + zIndexDelta,
    )
    .hazeEffect(state, style) {
        drawPageArea(HazeZIndex.contentOverlay + zIndexDelta)
        block?.invoke(this)
    }

@OptIn(ExperimentalHazeApi::class)
context(page: Page)
fun HazeEffectScope.drawPageArea(zIndex: Float) {
    canDrawArea = canDrawArea@{ area ->
        val key = area.key
        if (key !is HazeKey) return@canDrawArea false
        key.zIndex < zIndex && key.page == page
    }
}

object HazeZIndex {
    private var currentNegative = -1f
        get() = field.also { field -= 1f }

    private var currentNotNegative = 0f
        get() = field.also { field += 1f }

    val content = currentNotNegative
    val topBar = currentNotNegative
    val contentOverlay = currentNotNegative
    val navDisplay = currentNotNegative
    val bottomBar = currentNotNegative
    val app = currentNotNegative
}

data class HazeKey(
    val page: Page,
    val zIndex: Float,
)
