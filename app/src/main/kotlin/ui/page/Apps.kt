package top.ltfan.notdeveloper.ui.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.only
import top.ltfan.notdeveloper.ui.util.with
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

object Apps : Main() {
    override val navigationLabel = R.string.label_nav_apps
    override val navigationIcon = Icons.Default.Apps

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override val metadata: Map<String, Any> = ListDetailSceneStrategy.listPane(this)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    context(contentPadding: PaddingValues)
    override fun AppViewModel.Content() {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(navigationLabel))
                    },
                    windowInsets = AppWindowInsets.only { horizontal + top },
                    scrollBehavior = scrollBehavior,
                )
            },
            contentWindowInsets = AppWindowInsets,
        ) { scaffoldPadding ->
            val contentPadding = (contentPadding with scaffoldPadding) {
                start { plus }
                top { plus + 16.dp }
                end { plus }
                bottom { plus + 16.dp }
            }


        }
    }
}
