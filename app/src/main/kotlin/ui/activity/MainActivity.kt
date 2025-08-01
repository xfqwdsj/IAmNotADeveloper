package top.ltfan.notdeveloper.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.stringResource
import androidx.navigation3.ui.NavDisplay
import top.ltfan.notdeveloper.ui.page.Main
import top.ltfan.notdeveloper.ui.theme.IAmNotADeveloperTheme
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.only
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel
import top.ltfan.notdeveloper.util.isMiui
import top.ltfan.notdeveloper.xposed.notDevService
import top.ltfan.notdeveloper.xposed.statusIsPreferencesReady
import kotlin.math.max
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
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
            IAmNotADeveloperTheme {
                val insets = AppWindowInsets
                val navBarHeightFactor by animateFloatAsState(if (viewModel.showNavBar) 1f else 0f)
                SubcomposeLayout { constraints ->
                    val insetsBottom = insets.getBottom(this)

                    val navBar = subcompose("navBar") {
                        NavigationBar(
                            windowInsets = insets.only { horizontal + bottom }
                        ) {
                            Main.pages.forEach { page ->
                                NavigationBarItem(
                                    selected = viewModel.navBarEntry == page,
                                    onClick = {
                                        viewModel.navigateMain(page)
                                    },
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

                    val contentPlaceable = subcompose("content") {
                        NavDisplay(
                            backStack = viewModel.backStack,
                            modifier = Modifier.consumeWindowInsets(insets.only { bottom }),
                            entryProvider = { it.navEntry(viewModel, contentPadding) },
                        )
                    }.first().measure(constraints)

                    layout(constraints.maxWidth, constraints.maxHeight) {
                        contentPlaceable.place(0, 0)
                        navBarPlaceable?.place(0, navBarY!!)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.isPreferencesReady = statusIsPreferencesReady
        if (viewModel.service == null) {
            viewModel.service = notDevService
        }
        viewModel.test()
    }
}
