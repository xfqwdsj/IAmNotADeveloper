package top.ltfan.notdeveloper.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import androidx.navigation3.ui.NavDisplay
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import top.ltfan.material.m3.core.visual.LocalBackdrop
import top.ltfan.material.m3.core.visual.LocalBlurEnabled
import top.ltfan.material.m3.core.visual.captureBackdrop
import top.ltfan.material.m3.core.visual.rememberBackdropLayer
import top.ltfan.material.m3.overlay.OverlayHost
import top.ltfan.material.m3.overlay.OverlayHostState
import top.ltfan.notdeveloper.application.NotDevApplication
import top.ltfan.notdeveloper.ui.composable.LiquidBottomTab
import top.ltfan.notdeveloper.ui.composable.LiquidBottomTabs
import top.ltfan.notdeveloper.ui.page.Main
import top.ltfan.notdeveloper.ui.theme.IAmNotADeveloperTheme
import top.ltfan.notdeveloper.ui.util.LocalBottomBarHeight
import top.ltfan.notdeveloper.ui.util.only
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel
import top.ltfan.notdeveloper.util.isMiui

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels {
        viewModelFactory {
            addInitializer(AppViewModel::class) {
                AppViewModel(this@MainActivity.application as NotDevApplication)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        @Suppress("DEPRECATION") if (isMiui) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
        }
        super.onCreate(savedInstanceState)

        setContent {
            IAmNotADeveloperTheme(viewModel) {
                val vm = this
                val background = MaterialTheme.colorScheme.background
                val backdrop = rememberBackdropLayer(background)
                val overlayHost = remember { OverlayHostState() }
                val blurEnabled = blur
                val bottomBarHeight = remember { mutableStateOf(0.dp) }
                CompositionLocalProvider(
                    LocalBackdrop provides backdrop,
                    LocalBlurEnabled provides blurEnabled,
                ) {
                    OverlayHost(overlayHost) {
                        CompositionLocalProvider(
                            LocalBottomBarHeight provides
                                    if (vm.showNavBar) bottomBarHeight.value else 0.dp,
                        ) {
                            NavDisplay(
                                backStack = backStack,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .captureBackdrop(backdrop),
                                entryDecorators = listOf(
                                    rememberSaveableStateHolderNavEntryDecorator(),
                                    rememberViewModelStoreNavEntryDecorator(),
                                ),
                                sceneDecoratorStrategies = listOf(
                                    BottomBarSceneDecorator(bottomBarHeight) { sceneBackdrop ->
                                        if (vm.showNavBar) BottomBar(vm, sceneBackdrop)
                                    },
                                ),
                                entryProvider = { it.navEntry() },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws the floating bottom bar over the scene content and exposes its
 * measured height through [LocalBottomBarHeight] so the pages can reserve
 * room for it in their own content padding. The scene content itself is
 * left full-size (edge to edge), so page-level scrims cover the whole
 * window.
 *
 * The scene content is captured into a scene-scoped backdrop that does
 * not contain the bar, and the bar samples that backdrop as a sibling.
 * Sampling a backdrop from inside its own captured subtree would make
 * the layer draw itself while recording, so the two subtrees must
 * stay disjoint. The app-scoped backdrop (captured around the whole
 * `NavDisplay`) is left for the overlays.
 */
private class BottomBarSceneDecorator<T : Any>(
    private val barHeight: MutableState<Dp>,
    private val bottomBar: @Composable (LayerBackdrop) -> Unit,
) : SceneDecoratorStrategy<T> {
    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> =
        DecoratedScene(scene, barHeight, bottomBar)
}

private class DecoratedScene<T : Any>(
    private val scene: Scene<T>,
    private val barHeight: MutableState<Dp>,
    private val bottomBar: @Composable (LayerBackdrop) -> Unit,
) : Scene<T> {
    override val key get() = scene.key
    override val entries get() = scene.entries
    override val previousEntries get() = scene.previousEntries
    override val metadata get() = scene.metadata

    override fun equals(other: Any?): Boolean =
        other is DecoratedScene<*> && other.scene == scene

    override fun hashCode(): Int = scene.hashCode()

    override val content: @Composable () -> Unit = {
        val background = MaterialTheme.colorScheme.background
        val sceneBackdrop = rememberLayerBackdrop {
            drawRect(background)
            drawContent()
        }
        val density = LocalDensity.current
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .layerBackdrop(sceneBackdrop)
            ) {
                scene.content()
            }
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only { bottom })
            ) {
                Box(
                    Modifier.onGloballyPositioned {
                        barHeight.value = with(density) { it.size.height.toDp() }
                    }
                ) {
                    bottomBar(sceneBackdrop)
                }
            }
        }
    }
}

@Composable
private fun BottomBar(viewModel: AppViewModel, backdrop: LayerBackdrop) {
    val pages = Main.pages
    LiquidBottomTabs(
        selectedTabIndex = { pages.indexOf(viewModel.navBarEntry).coerceAtLeast(0) },
        onTabSelected = { index -> viewModel.navigateMain(pages[index]) },
        backdrop = backdrop,
        tabsCount = pages.size,
        modifier = Modifier.padding(horizontal = 28.dp),
    ) {
        pages.forEach { page ->
            LiquidBottomTab(
                onClick = { viewModel.navigateMain(page) },
                modifier = Modifier.defaultMinSize(minWidth = 76.dp),
            ) {
                Icon(
                    painterResource(page.navigationIcon),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(page.navigationLabel),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Visible,
                )
            }
        }
    }
}
