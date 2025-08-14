package top.ltfan.notdeveloper.ui.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.ui.composable.GroupedLazyColumn
import top.ltfan.notdeveloper.ui.composable.StatusCard
import top.ltfan.notdeveloper.ui.composable.categoryCards
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.HazeZIndex
import top.ltfan.notdeveloper.ui.util.LargeTopAppBarColorsTransparent
import top.ltfan.notdeveloper.ui.util.appBarHazeEffect
import top.ltfan.notdeveloper.ui.util.contentHazeSource
import top.ltfan.notdeveloper.ui.util.hazeSource
import top.ltfan.notdeveloper.ui.util.only
import top.ltfan.notdeveloper.ui.util.operate
import top.ltfan.notdeveloper.ui.util.plus
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

object Overview : Main() {
    override val navigationLabel: Int = R.string.label_nav_overview
    override val navigationIcon = Icons.Default.Home

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override val metadata: Map<String, Any> = ListDetailSceneStrategy.listPane(this)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    context(contentPadding: PaddingValues)
    override fun AppViewModel.Content() {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(stringResource(R.string.app_name))
                    },
                    modifier = Modifier
                        .hazeSource(zIndex = HazeZIndex.topBar)
                        .appBarHazeEffect(),
                    windowInsets = AppWindowInsets.only { horizontal + top },
                    scrollBehavior = scrollBehavior,
                    colors = LargeTopAppBarColorsTransparent,
                )
            },
            contentWindowInsets = AppWindowInsets + contentPadding,
        ) { contentPadding ->
            val contentPadding = contentPadding.operate {
                top += 16.dp
                bottom += 16.dp
            }

            GroupedLazyColumn(
                modifier = Modifier
                    .contentHazeSource()
                    .consumeWindowInsets(contentPadding)
                    .fillMaxSize(),
                contentPadding = contentPadding,
                spacing = 16.dp,
            ) {
                group {
                    item {
                        StatusCard(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            isPreferencesReady = isPreferencesReady,
                            isServiceConnected = service != null,
                        )
                    }
                }

                categoryCards(
                    groups = DetectionCategory.values,
                    testResults = testResults,
                    afterChange = ::afterStatusChange,
                    isPreferencesReady = isPreferencesReady,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
