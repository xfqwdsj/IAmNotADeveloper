package top.ltfan.notdeveloper.ui.util

import androidx.compose.animation.core.CubicBezierEasing
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
    state: HazeState = viewModel.hazeState, zIndex: Float = 0f, key: Any? = null
) = hazeSource(state, zIndex, key)

context(viewModel: AppViewModel)
fun Modifier.hazeEffect(
    state: HazeState = viewModel.hazeState,
    style: HazeStyle = HazeStyle.Unspecified,
    block: (HazeEffectScope.() -> Unit)? = null,
) = hazeEffect(state, style, block)

@Composable
context(viewModel: AppViewModel)
fun Modifier.hazeEffectTop(
    state: HazeState = viewModel.hazeState,
    style: HazeStyle = HazeStyleAppBar,
    block: (HazeEffectScope.() -> Unit)? = null,
) = hazeEffect(state, style) {
    progressive = HazeProgressive.verticalGradient(
        easing = HazeEasing,
        startY = Float.POSITIVE_INFINITY,
        endY = 0f,
    )
    block?.invoke(this)
}

@Composable
context(viewModel: AppViewModel)
fun Modifier.hazeEffectBottom(
    state: HazeState = viewModel.hazeState,
    style: HazeStyle = HazeStyleAppBar,
    block: (HazeEffectScope.() -> Unit)? = null,
) = hazeEffect(state, style) {
    progressive = HazeProgressive.verticalGradient(easing = HazeEasing)
    block?.invoke(this)
}

context(viewModel: AppViewModel, page: Page)
fun Modifier.contentHazeSource(state: HazeState = viewModel.hazeState) = hazeSource(
    state = state,
    zIndex = HazeZIndex.content,
    key = page,
)

@OptIn(ExperimentalHazeApi::class)
@Composable
context(viewModel: AppViewModel, page: Page)
fun Modifier.appBarHazeEffect(
    state: HazeState = viewModel.hazeState,
    style: HazeStyle = HazeStyleAppBar,
    block: (HazeEffectScope.() -> Unit)? = null,
) = hazeEffectTop(state, style) {
    canDrawArea = { area ->
        area.key == page
    }
    block?.invoke(this)
}

object HazeZIndex {
    private var currentNegative = -1f
        get() = field.also { field -= 1f }

    private var currentNotNegative = 0f
        get() = field.also { field += 1f }

    val content = currentNotNegative
    val topBar = currentNotNegative
    val navDisplay = currentNotNegative
    val bottomBar = currentNotNegative
    val app = currentNotNegative
}
