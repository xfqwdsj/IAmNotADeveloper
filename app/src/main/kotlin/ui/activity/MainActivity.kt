package top.ltfan.notdeveloper.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import androidx.navigation3.ui.NavDisplay
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.capsule.ContinuousCapsule
import top.ltfan.material.m3.core.visual.BackdropLayerHandle
import top.ltfan.material.m3.core.visual.LocalBackdrop
import top.ltfan.material.m3.core.visual.LocalBlurEnabled
import top.ltfan.material.m3.core.visual.backdropSurface
import top.ltfan.material.m3.core.visual.captureBackdrop
import top.ltfan.material.m3.core.visual.rememberBackdropLayer
import top.ltfan.material.m3.overlay.OverlayHost
import top.ltfan.material.m3.overlay.OverlayHostState
import top.ltfan.notdeveloper.application.NotDevApplication
import top.ltfan.notdeveloper.datastore.UiSettings
import top.ltfan.notdeveloper.ui.page.Main
import top.ltfan.notdeveloper.ui.theme.IAmNotADeveloperTheme
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
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
                val blurEnabled = blurSettings is UiSettings.BlurSettings.Value.Enabled
                CompositionLocalProvider(
                    LocalBackdrop provides backdrop,
                    LocalBlurEnabled provides blurEnabled,
                ) {
                    OverlayHost(overlayHost) {
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
                                BottomBarSceneDecorator {
                                    if (vm.showNavBar) BottomBar(vm, backdrop)
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

/**
 * Draws the floating bottom bar over the scene content and reserves its
 * height at the bottom of the content.
 */
private class BottomBarSceneDecorator<T : Any>(
    private val bottomBar: @Composable () -> Unit,
) : SceneDecoratorStrategy<T> {
    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> =
        DecoratedScene(scene, bottomBar)
}

private class DecoratedScene<T : Any>(
    private val scene: Scene<T>,
    private val bottomBar: @Composable () -> Unit,
) : Scene<T> {
    override val key get() = scene.key
    override val entries get() = scene.entries
    override val previousEntries get() = scene.previousEntries
    override val metadata get() = scene.metadata

    override val content: @Composable () -> Unit = {
        var barHeight by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current
        val animatedBarHeight by animateDpAsState(barHeight)
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = animatedBarHeight)
            ) {
                scene.content()
            }
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(AppWindowInsets.only { bottom })
            ) {
                Box(
                    Modifier.onGloballyPositioned {
                        barHeight = with(density) { it.size.height.toDp() }
                    }
                ) {
                    bottomBar()
                }
            }
        }
    }
}

@Composable
private fun BottomBar(viewModel: AppViewModel, backdrop: BackdropLayerHandle) {
    val surface = MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .backdropSurface(
                handle = backdrop,
                shape = ContinuousCapsule,
                effects = {
                    vibrancy()
                    blur(8.dp.toPx())
                },
                onDrawSurface = { drawRect(surface.copy(alpha = 0.6f)) },
            ),
    ) {
        Row(Modifier.fillMaxWidth()) {
            Main.pages.forEach { page ->
                val selected = viewModel.navBarEntry == page
                val contentColor =
                    if (selected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateMain(page) }
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (selected) {
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .padding(horizontal = 4.dp)
                                    .backdropSurface(
                                        handle = backdrop,
                                        shape = ContinuousCapsule,
                                        effects = {
                                            lens(
                                                refractionHeight = 12.dp.toPx(),
                                                refractionAmount = 16.dp.toPx(),
                                                depthEffect = true,
                                                chromaticAberration = true,
                                            )
                                        },
                                        highlight = null,
                                        shadow = null,
                                        onDrawSurface = { drawRect(contentColor.copy(alpha = 0.1f)) },
                                    ),
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(page.navigationIcon, contentDescription = null, tint = contentColor)
                            Text(
                                text = stringResource(page.navigationLabel),
                                color = contentColor,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}
