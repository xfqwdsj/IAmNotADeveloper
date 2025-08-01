package top.ltfan.notdeveloper.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.ui.NavDisplay
import top.ltfan.notdeveloper.ui.page.Main
import top.ltfan.notdeveloper.ui.theme.IAmNotADeveloperTheme
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel
import top.ltfan.notdeveloper.util.isMiui
import top.ltfan.notdeveloper.xposed.notDevService
import top.ltfan.notdeveloper.xposed.statusIsPreferencesReady

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
                Column {
                    Scaffold(
                        topBar = {
                            var lastLabel by remember { mutableStateOf(viewModel.currentPage.appBarLabel) }

                            LaunchedEffect(viewModel.currentPage.appBarLabel) {
                                if (viewModel.currentPage.appBarLabel != null) {
                                    lastLabel = viewModel.currentPage.appBarLabel
                                }
                            }

                            AnimatedVisibility(
                                visible = viewModel.currentPage.appBarLabel != null,
                                enter = expandVertically(),
                                exit = shrinkVertically(),
                            ) {
                                TopAppBar(
                                    title = { Text(stringResource(lastLabel!!)) }
                                )
                            }
                        },
                        bottomBar = {
                            NavigationBar {
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
                        },
                    ) { contentPadding ->
                        NavDisplay(
                            backStack = viewModel.backStack,
                            modifier = Modifier.weight(1f),
                            entryProvider = { it.navEntry(viewModel, contentPadding) },
                        )
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
