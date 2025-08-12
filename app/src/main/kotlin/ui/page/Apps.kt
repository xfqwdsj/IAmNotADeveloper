package top.ltfan.notdeveloper.ui.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.application
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.ui.composable.AppListItem
import top.ltfan.notdeveloper.ui.composable.GroupedLazyColumn
import top.ltfan.notdeveloper.ui.composable.HazeAlertDialog
import top.ltfan.notdeveloper.ui.composable.card
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.CardColorsLowest
import top.ltfan.notdeveloper.ui.util.HazeZIndex
import top.ltfan.notdeveloper.ui.util.TopAppBarColorsTransparent
import top.ltfan.notdeveloper.ui.util.appBarHazeEffect
import top.ltfan.notdeveloper.ui.util.contentHazeSource
import top.ltfan.notdeveloper.ui.util.hazeSource
import top.ltfan.notdeveloper.ui.util.only
import top.ltfan.notdeveloper.ui.util.operate
import top.ltfan.notdeveloper.ui.util.plus
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

object Apps : Main() {
    override val navigationLabel = R.string.label_nav_apps
    override val navigationIcon = Icons.Default.Apps

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override val metadata: Map<String, Any> = ListDetailSceneStrategy.listPane(this)

    @OptIn(ExperimentalMaterial3Api::class)
    val topAppBarState = TopAppBarState(
        initialHeightOffsetLimit = -Float.MAX_VALUE,
        initialHeightOffset = 0f,
        initialContentOffset = 0f,
    )
    val lazyListState = LazyListState()

    var isAppListError by mutableStateOf(false)
    var showAppListErrorInfoDialog by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    context(contentPadding: PaddingValues)
    override fun AppViewModel.Content() {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
            state = topAppBarState,
        )
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(navigationLabel))
                    },
                    modifier = Modifier
                        .hazeSource(zIndex = HazeZIndex.topBar)
                        .appBarHazeEffect(),
                    actions = {
                        if (isAppListError) {
                            IconButton(
                                onClick = { showAppListErrorInfoDialog = true },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = stringResource(R.string.action_apps_info_query_show),
                                )
                            }

                            if (showAppListErrorInfoDialog) {
                                HazeAlertDialog(
                                    onDismissRequest = { showAppListErrorInfoDialog = false },
                                    confirmButton = {
                                        TextButton(
                                            onClick = { showAppListErrorInfoDialog = false },
                                        ) {
                                            Text(stringResource(android.R.string.ok))
                                        }
                                    },
                                    title = {
                                        Text(stringResource(R.string.title_dialog_apps_query_failed))
                                    },
                                    text = {
                                        Text(stringResource(R.string.message_dialog_apps_query_failed))
                                    },
                                )
                            }
                        }
                    },
                    windowInsets = AppWindowInsets.only { horizontal + top },
                    colors = TopAppBarColorsTransparent,
                    scrollBehavior = scrollBehavior,
                )
            },
            contentWindowInsets = AppWindowInsets + contentPadding,
        ) { contentPadding ->
            val contentPadding = contentPadding.operate {
                top += 16.dp
                bottom += 16.dp
            }

            val list = remember {
                service?.queryApps()?.ifEmpty { null }?.sortedBy { it.packageName }?.also {
                    isAppListError = false
                } ?: listOf(application.applicationInfo).also {
                    isAppListError = true
                }
            }

            val snackbarMessage = stringResource(R.string.message_snackbar_apps_query_failed)
            LaunchedEffect(isAppListError) {
                snackbarHostState.showSnackbar(snackbarMessage)
            }

            val cardColors = CardColorsLowest

            GroupedLazyColumn(
                modifier = Modifier
                    .contentHazeSource()
                    .consumeWindowInsets(contentPadding)
                    .fillMaxSize(),
                state = lazyListState,
                contentPadding = contentPadding,
            ) {
                card(
                    colors = cardColors,
                ) {
                    item(
                        key = "header-configured",
                        contentType = "header",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.label_list_header_apps_configured),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    items(
                        items = list,
                        key = { "${it.packageName}-${it.uid}" },
                        contentType = { "app" },
                        modifier = { Modifier.padding(horizontal = 16.dp) }
                    ) {
                        AppListItem(it)
                    }
                }
            }
        }
    }
}
