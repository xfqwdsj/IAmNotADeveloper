package top.ltfan.notdeveloper.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.ui.NavDisplay
import top.ltfan.notdeveloper.application.NotDevApplication
import top.ltfan.notdeveloper.service.systemService
import top.ltfan.notdeveloper.ui.composable.HazeSnackbar
import top.ltfan.notdeveloper.ui.composable.HazeSnackbarHost
import top.ltfan.notdeveloper.ui.page.Main
import top.ltfan.notdeveloper.ui.theme.IAmNotADeveloperTheme
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.HazeZIndex
import top.ltfan.notdeveloper.ui.util.hazeEffectBottom
import top.ltfan.notdeveloper.ui.util.hazeSource
import top.ltfan.notdeveloper.ui.util.only
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel
import top.ltfan.notdeveloper.util.isMiui
import top.ltfan.notdeveloper.xposed.statusIsPreferencesReady
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

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
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
                SubcomposeLayout { constraints ->
                    val width = constraints.maxWidth
                    val height = constraints.maxHeight

                    val insetsBottom = insets.getBottom(this)

                    val navBar = subcompose("navBar") {
                        NavigationBar(
                            modifier = Modifier
                                .hazeSource(zIndex = HazeZIndex.bottomBar)
                                .hazeEffectBottom(),
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            tonalElevation = 0.dp,
                            windowInsets = insets.only { horizontal + bottom }
                        ) {
                            Main.pages.forEach { page ->
                                NavigationBarItem(
                                    selected = navBarEntry == page,
                                    onClick = { navigateMain(page) },
                                    icon = {
                                        Icon(page.navigationIcon, contentDescription = null)
                                    },
                                    label = {
                                        Text(stringResource(page.navigationLabel))
                                    }
                                )
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

                    val snackbarHostPlaceable = subcompose("snackbarHost") {
                        Box(contentAlignment = Alignment.Center) {
                            HazeSnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                HazeSnackbar(
                                    snackbarData = it,
                                    modifier = Modifier.hazeSource(zIndex = HazeZIndex.bottomBar),
                                )
                            }
                        }
                    }.first().measure(constraints)

                    val snackbarHeight = snackbarHostPlaceable.height
                    val snackbarY = height - paddingBottom - snackbarHeight

                    val contentPlaceable = subcompose("content") {
                        NavDisplay(
                            backStack = backStack,
                            modifier = Modifier
                                .consumeWindowInsets(insets.only { bottom })
                                .hazeSource(zIndex = HazeZIndex.navDisplay),
                            // TODO: Can cause LazyList unscrollable issue when orientated from
                            // TODO: landscape to portrait. Uncomment when fixed.
//                            sceneStrategy = rememberListDetailSceneStrategy(),
                            entryProvider = { it.navEntry(contentPadding) },
                        )
                    }.first().measure(constraints)

                    layout(width, height) {
                        contentPlaceable.place(0, 0)
                        navBarPlaceable?.place(0, navBarY!!)
                        snackbarHostPlaceable.place(0, snackbarY)
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
