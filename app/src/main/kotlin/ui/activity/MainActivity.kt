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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.kyant.backdrop.backdrops.LayerBackdrop
import top.ltfan.material.m3.core.layout.only
import top.ltfan.material.m3.core.visual.LocalBackdrop
import top.ltfan.material.m3.core.visual.LocalBlurEnabled
import top.ltfan.material.m3.core.visual.captureBackdrop
import top.ltfan.material.m3.core.visual.rememberBackdropLayer
import top.ltfan.material.m3.overlay.OverlayHost
import top.ltfan.material.m3.overlay.OverlayHostState
import top.ltfan.notdeveloper.application.NotDevApplication
import top.ltfan.notdeveloper.ui.composable.FloatingBottomBar
import top.ltfan.notdeveloper.ui.composable.FloatingBottomBarItem
import top.ltfan.notdeveloper.ui.page.Main
import top.ltfan.notdeveloper.ui.theme.IAmNotADeveloperTheme
import top.ltfan.notdeveloper.ui.util.LocalBottomBarHeight
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
                val density = LocalDensity.current
                CompositionLocalProvider(
                    LocalBackdrop provides backdrop,
                    LocalBlurEnabled provides blurEnabled,
                ) {
                    OverlayHost(overlayHost) {
                        Box(Modifier.fillMaxSize()) {
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
                                    entryProvider = { it.navEntry() },
                                )
                            }
                            if (vm.showNavBar) {
                                Box(
                                    Modifier
                                        .align(Alignment.BottomCenter)
                                        .windowInsetsPadding(WindowInsets.safeDrawing.only { bottom })
                                ) {
                                    Box(
                                        Modifier.onGloballyPositioned {
                                            bottomBarHeight.value =
                                                with(density) { it.size.height.toDp() }
                                        }
                                    ) {
                                        BottomBar(vm, backdrop.layer)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The floating bottom bar is hosted as a sibling of [NavDisplay] rather
 * than inside a scene, so it is composed once and stays fixed while the
 * scenes cross-fade underneath it. Its measured height is published
 * through [LocalBottomBarHeight] for the pages to reserve as content
 * padding. The app-scoped backdrop captures the [NavDisplay] content
 * alone, so the sibling bar samples a layer that does not contain itself.
 */
@Composable
private fun BottomBar(viewModel: AppViewModel, backdrop: LayerBackdrop) {
    val pages = Main.pages
    val selectedIndex = pages.indexOf(viewModel.navBarEntry).coerceAtLeast(0)
    FloatingBottomBar(
        selectedTabIndex = { selectedIndex },
        onTabSelected = { index -> viewModel.navigateMain(pages[index]) },
        backdrop = backdrop,
        tabsCount = pages.size,
        modifier = Modifier.padding(horizontal = 28.dp),
    ) {
        pages.forEach { page ->
            FloatingBottomBarItem(
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
