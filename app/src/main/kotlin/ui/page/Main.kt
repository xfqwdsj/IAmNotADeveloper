package top.ltfan.notdeveloper.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.ui.composable.CategoryCard
import top.ltfan.notdeveloper.ui.composable.StatusCard
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@Serializable
object Main : Page() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun AppViewModel.Content() {
        val scrollBehavior =
            TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(stringResource(R.string.app_name))
                    },
                    windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                    scrollBehavior = scrollBehavior
                )
            },
        ) { padding ->
            val layoutDirection = LocalLayoutDirection.current
            val insets = WindowInsets.displayCutout
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                .asPaddingValues()
            val contentPadding = PaddingValues(
                start = padding.calculateStartPadding(layoutDirection) + insets.calculateStartPadding(
                    layoutDirection
                ),
                top = padding.calculateTopPadding() + insets.calculateTopPadding() + 16.dp,
                end = padding.calculateEndPadding(layoutDirection) + insets.calculateEndPadding(
                    layoutDirection
                ),
                bottom = padding.calculateBottomPadding() + insets.calculateBottomPadding() + 16.dp,
            )
            LazyColumn(
                modifier = Modifier
                    .consumeWindowInsets(contentPadding)
                    .fillMaxSize(),
                contentPadding = contentPadding,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    StatusCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        isPreferencesReady = isPreferencesReady,
                        isServiceConnected = service != null,
                    )
                }

                items(DetectionCategory.values) { category ->
                    CategoryCard(
                        category = category,
                        testResults = testResults,
                        afterChange = ::afterStatusChange,
                        isPreferencesReady = isPreferencesReady,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}
