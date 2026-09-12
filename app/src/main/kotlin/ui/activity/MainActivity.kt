package top.ltfan.notdeveloper.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.capsule.ContinuousCapsule
import top.ltfan.notdeveloper.application.NotDevApplication
import top.ltfan.notdeveloper.ui.page.Main
import top.ltfan.notdeveloper.ui.theme.IAmNotADeveloperTheme
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.material.m3.core.visual.LocalBackdrop
import top.ltfan.material.m3.core.visual.backdropSurface
import top.ltfan.material.m3.core.visual.captureBackdrop
import top.ltfan.material.m3.core.visual.rememberBackdropLayer
import top.ltfan.material.m3.overlay.OverlayHost
import top.ltfan.material.m3.overlay.OverlayHostState
import top.ltfan.notdeveloper.ui.util.only
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel
import top.ltfan.notdeveloper.util.isMiui
import kotlin.math.max
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels {
        viewModelFactory {
            addInitializer(AppViewModel::class) {
                AppViewModel(this@MainActivity.application as NotDevApplication)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
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
                val insets = AppWindowInsets
                val navBarHeightFactor by animateFloatAsState(if (showNavBar) 1f else 0f)
                val background = MaterialTheme.colorScheme.background
                val surface = MaterialTheme.colorScheme.surface
                val backdrop = rememberBackdropLayer(background)
                val overlayHost = remember { OverlayHostState() }
                CompositionLocalProvider(LocalBackdrop provides backdrop) {
                OverlayHost(overlayHost) {
                SubcomposeLayout { constraints ->
                    val width = constraints.maxWidth
                    val height = constraints.maxHeight

                    val insetsBottom = insets.getBottom(this)

                    val navBar = subcompose("navBar") {
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
                                    val selected = navBarEntry == page
                                    val contentColor =
                                        if (selected) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { navigateMain(page) }
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
                                                            onDrawSurface = {
                                                                drawRect(contentColor.copy(alpha = 0.1f))
                                                            },
                                                        ),
                                                )
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                Icon(
                                                    page.navigationIcon,
                                                    contentDescription = null,
                                                    tint = contentColor,
                                                )
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

                    val navBarPlaceable = if (navBarHeightFactor != 0f) {
                        navBar.first().measure(constraints)
                    } else {
                        null
                    }

                    val navBarHeight =
                        navBarPlaceable?.height?.times(navBarHeightFactor)?.roundToInt()
                    val navBarY = navBarHeight?.let { constraints.maxHeight - it }

                    val paddingBottom = navBarHeight?.let { max(insetsBottom, it) } ?: insetsBottom
                    val contentPadding = PaddingValues(bottom = paddingBottom.toDp())

                    val contentPlaceable = subcompose("content") {
                        NavDisplay(
                            backStack = backStack,
                            modifier = Modifier
                                .consumeWindowInsets(insets.only { bottom })
                                .captureBackdrop(backdrop),
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator(),
                            ),
                            // TODO: Can cause LazyList unscrollable issue when orientated from
                            // TODO: landscape to portrait. Uncomment when fixed.
//                            sceneStrategy = rememberListDetailSceneStrategy(),
                            entryProvider = { it.navEntry(contentPadding) },
                        )
                    }.first().measure(constraints)

                    layout(width, height) {
                        contentPlaceable.place(0, 0)
                        navBarPlaceable?.place(0, navBarY!!)
                    }
                }
                }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }
}
