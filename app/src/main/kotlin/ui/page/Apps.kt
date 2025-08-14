package top.ltfan.notdeveloper.ui.page

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.ui.composable.AppListItem
import top.ltfan.notdeveloper.ui.composable.GroupedLazyColumn
import top.ltfan.notdeveloper.ui.composable.HazeAlertDialog
import top.ltfan.notdeveloper.ui.composable.IconButtonWithTooltip
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

    var sortMethod by mutableStateOf(Sort.Label)
    var filteredMethods = mutableStateSetOf<Filter>()

    var isAppListError by mutableStateOf(false)
    var showAppListErrorInfoDialog by mutableStateOf(false)

    var showFilterBottomSheet by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    context(contentPadding: PaddingValues)
    override fun AppViewModel.Content() {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
        val myPackageInfo =
            remember { application.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0) }
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
                            IconButtonWithTooltip(
                                imageVector = Icons.Default.Warning,
                                contentDescription = R.string.action_apps_query_details_show,
                                onClick = { showAppListErrorInfoDialog = true },
                            )

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

                        IconButtonWithTooltip(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = R.string.action_bottom_sheet_apps_filter_show,
                            onClick = { showFilterBottomSheet = true },
                        )
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

            val appList = remember {
                service?.queryApps()?.ifEmpty { null }?.sortedBy { it.packageName }?.also {
                    isAppListError = false
                } ?: listOf(myPackageInfo).also {
                    isAppListError = true
                }
            }

            val snackbarMessage = stringResource(R.string.message_snackbar_apps_query_failed)
            LaunchedEffect(isAppListError) {
                if (isAppListError) {
                    snackbarHostState.showSnackbar(snackbarMessage)
                }
            }

            GroupedLazyColumn(
                modifier = Modifier
                    .contentHazeSource()
                    .consumeWindowInsets(contentPadding)
                    .fillMaxSize(),
                state = lazyListState,
                contentPadding = contentPadding,
            ) {
                card(
                    colors = { CardColorsLowest },
                ) {
                    header(
                        text = R.string.label_list_header_apps_unconfigured,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    items(
                        items = appList,
                        key = { "${it.packageName}-${it.applicationInfo?.uid}" },
                        contentType = { "app" },
                        modifier = { Modifier.padding(horizontal = 16.dp) }
                    ) {
                        AppListItem(it)
                    }
                }
            }

            if (showFilterBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showFilterBottomSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                ) {
                    Text(
                        text = stringResource(R.string.title_bottom_sheet_apps_filter),
                        modifier = Modifier.padding(horizontal = 24.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.label_bottom_sheet_apps_filter_sort),
                        modifier = Modifier.padding(horizontal = 20.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Sort.entries.forEach {
                            val selected = sortMethod == it
                            FilterChip(
                                selected = selected,
                                onClick = { sortMethod = it },
                                label = { Text(stringResource(it.labelRes)) },
                                leadingIcon = {
                                    AnimatedVisibility(selected) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.label_bottom_sheet_apps_filter),
                        modifier = Modifier.padding(horizontal = 20.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val allSelected = filteredMethods.isEmpty()
                        FilterChip(
                            selected = allSelected,
                            onClick = {
                                if (allSelected) {
                                    filteredMethods.clear()
                                    filteredMethods.addAll(Filter.usableEntries)
                                } else {
                                    filteredMethods.clear()
                                }
                            },
                            label = { Text(stringResource(Filter.All.labelRes)) },
                            leadingIcon = {
                                AnimatedContent(allSelected) { allSelected ->
                                    Icon(
                                        imageVector = if (allSelected) Icons.Default.Check else Icons.Default.Remove,
                                        contentDescription = null,
                                    )
                                }
                            },
                        )
                        Filter.usableEntries.forEach {
                            val selected = !filteredMethods.contains(it)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    if (filteredMethods.contains(it)) {
                                        filteredMethods.remove(it)
                                    } else {
                                        filteredMethods.add(it)
                                    }
                                },
                                label = { Text(stringResource(it.labelRes)) },
                                leadingIcon = {
                                    AnimatedContent(selected) { selected ->
                                        Icon(
                                            imageVector = if (selected) Icons.Default.Check else Icons.Default.Remove,
                                            contentDescription = null,
                                        )
                                    }
                                },
                            )
                        }
                    }
                    Spacer(Modifier.height(64.dp))
                }
            }
        }
    }

    enum class Sort(@param:StringRes val labelRes: Int) {
        Label(R.string.item_apps_filter_sort_label),
        Package(R.string.item_apps_filter_sort_package),
        Updated(R.string.item_apps_filter_sort_updated);
    }

    enum class Filter(@param:StringRes val labelRes: Int) {
        All(R.string.item_apps_filter_all),
        Configured(R.string.item_apps_filter_configured),
        Unconfigured(R.string.item_apps_filter_unconfigured),
        System(R.string.item_apps_filter_system);

        companion object {
            val usableEntries = Filter.entries.drop(1)
        }
    }
}
