package top.ltfan.notdeveloper.ui.page

import android.content.pm.PackageInfo
import android.os.UserHandle
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.datastore.AppFilter
import top.ltfan.notdeveloper.datastore.AppSort
import top.ltfan.notdeveloper.datastore.util.rememberPropertyAsState
import top.ltfan.notdeveloper.ui.composable.AnimatedVisibilityWithBlur
import top.ltfan.notdeveloper.ui.composable.AppListItem
import top.ltfan.notdeveloper.ui.composable.GroupedLazyColumn
import top.ltfan.notdeveloper.ui.composable.GroupedLazyListScope
import top.ltfan.notdeveloper.ui.composable.HazeAlertDialog
import top.ltfan.notdeveloper.ui.composable.IconButtonSizedIcon
import top.ltfan.notdeveloper.ui.composable.IconButtonWithTooltip
import top.ltfan.notdeveloper.ui.composable.card
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.CardColorsLowest
import top.ltfan.notdeveloper.ui.util.LinearMaskData
import top.ltfan.notdeveloper.ui.util.TopAppBarColorsTransparent
import top.ltfan.notdeveloper.ui.util.appBarHaze
import top.ltfan.notdeveloper.ui.util.contentHazeSource
import top.ltfan.notdeveloper.ui.util.horizontalAlphaMaskLinear
import top.ltfan.notdeveloper.ui.util.only
import top.ltfan.notdeveloper.ui.util.operate
import top.ltfan.notdeveloper.ui.util.plus
import top.ltfan.notdeveloper.ui.util.rememberAutoRestorableState
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel
import top.ltfan.notdeveloper.util.mutableProperty
import kotlin.reflect.full.staticFunctions

object Apps : Main() {
    override val navigationLabel = R.string.label_nav_apps
    override val navigationIcon = Icons.Default.Apps

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override val metadata: Map<String, Any> = ListDetailSceneStrategy.listPane(this)

    val lazyListState = LazyListState()

    var fullList by mutableStateOf(listOf<PackageInfo>())
    var configuredList by mutableStateOf(listOf<PackageInfo>())
    var unconfiguredList by mutableStateOf(listOf<PackageInfo>())

    var showUserFilter by mutableStateOf(false)

    var currentConfiguringPackageInfo by mutableStateOf<PackageInfo?>(null)

    var isAppListError by mutableStateOf(false)
    var showAppListErrorInfoDialog by mutableStateOf(false)

    var showFilterBottomSheet by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    @Composable
    context(contentPadding: PaddingValues)
    override fun AppViewModel.Content() {
        var selectedUser by settingsStore.rememberPropertyAsState(
            get = { it.selectedUser },
            set = { settings, user -> settings.copy(selectedUser = user) },
        )

        var sortMethod by settingsStore.rememberPropertyAsState(
            get = { it.sort },
            set = { settings, sort -> settings.copy(sort = sort) },
        )

        var filteredMethods by settingsStore.rememberPropertyAsState(
            get = { it.filtered },
            set = { settings, filters -> settings.copy(filtered = filters) },
        )

        val databaseList by application.database.dao().getPackageInfoFlow()
            .collectAsStateWithLifecycle(listOf())

        val snackbarMessage = stringResource(R.string.message_snackbar_apps_query_failed)

        SharedTransitionLayout {
            Scaffold(
                topBar = {
                    Column(Modifier.appBarHaze()) {
                        TopAppBar(
                            title = { Text(stringResource(navigationLabel)) },
                            actions = { AppBarActions() },
                            windowInsets = AppWindowInsets.only { horizontal + top },
                            colors = TopAppBarColorsTransparent,
                        )
                        FilterBar(
                            selectedUser,
                            { selectedUser = it },
                        )
                    }
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
                        .fillMaxSize(),
                    state = lazyListState,
                    contentPadding = contentPadding,
                ) {
                    appList(
                        list = configuredList,
                        header = R.string.label_list_header_apps_configured,
                    )

                    appList(
                        list = unconfiguredList,
                        header = R.string.label_list_header_apps_unconfigured,
                    )
                }

                NoAppsBackground(
                    status = AppListStatus(
                        isEmpty = configuredList.isEmpty() && unconfiguredList.isEmpty(),
                        isFiltered = filteredMethods.isNotEmpty(),
                    ),
                )

                FilterBottomSheet(
                    sortMethod,
                    { sortMethod = it },
                    filteredMethods,
                    { filteredMethods = it },
                )
            }

            AnimatedContent(
                targetState = currentConfiguringPackageInfo,
            ) { currentConfiguringPackageInfo ->
                if (currentConfiguringPackageInfo != null) {
                    AppConfiguration(
                        packageInfo = currentConfiguringPackageInfo,
                        dismiss = { Apps.currentConfiguringPackageInfo = null },
                    )
                }
            }
        }

        LaunchedEffect(users, selectedUser) {
            if (selectedUser in users) return@LaunchedEffect
            selectedUser = users.first()
        }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                fullList = (service?.queryApps()?.ifEmpty { null }?.also {
                    isAppListError = false
                } ?: listOf(myPackageInfo).also {
                    isAppListError = true
                })
            }
        }

        LaunchedEffect(fullList, databaseList, sortMethod, filteredMethods.size) {
            val queriedList = service?.queryApps(databaseList) ?: emptyList()
            val groupFilters = listOf(AppFilter.Configured, AppFilter.Unconfigured)
            val filters = filteredMethods.subtract(groupFilters)
            withContext(Dispatchers.IO) {
                configuredList = if (AppFilter.Configured !in filteredMethods) {
                    queriedList.processed(sortMethod, filters)
                } else emptyList()
                unconfiguredList = if (AppFilter.Unconfigured !in filteredMethods) {
                    fullList.filter { it !in configuredList }.processed(sortMethod, filters)
                } else emptyList()
            }
        }

        LaunchedEffect(isAppListError) {
            if (isAppListError) {
                snackbarHostState.showSnackbar(snackbarMessage)
            }
        }

        DisposableEffect(currentConfiguringPackageInfo) {
            showNavBar = if (currentConfiguringPackageInfo != null) {
                false
            } else {
                true
            }

            onDispose {
                showNavBar = true
            }
        }
    }

    @Composable
    context(viewModel: AppViewModel)
    fun RowScope.AppBarActions() {
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

        AnimatedContent(
            targetState = showUserFilter,
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = EnterTransition.None,
                    initialContentExit = ExitTransition.None,
                    sizeTransform = null,
                )
            }
        ) { showing ->
            val rotation by transition.animateFloat(
                label = "UserFilterRotation",
                transitionSpec = { tween(durationMillis = 300) },
            ) {
                val factor = if (showUserFilter) 1f else -1f
                when (it) {
                    EnterExitState.PreEnter -> -180f * factor
                    EnterExitState.Visible -> 0f
                    EnterExitState.PostExit -> 180f * factor
                }
            }
            if (showing) {
                IconButtonWithTooltip(
                    imageVector = Icons.Default.ExpandLess,
                    contentDescription = R.string.action_apps_user_select_hide,
                    modifier = Modifier.rotate(rotation),
                    onClick = { showUserFilter = false },
                )
            } else {
                IconButtonWithTooltip(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = R.string.action_apps_user_select_show,
                    modifier = Modifier.rotate(rotation),
                    onClick = { showUserFilter = true },
                )
            }
        }

        IconButtonWithTooltip(
            imageVector = Icons.Default.FilterList,
            contentDescription = R.string.action_bottom_sheet_apps_filter_show,
            onClick = { showFilterBottomSheet = true },
        )
    }

    @Composable
    context(viewModel: AppViewModel)
    fun ColumnScope.FilterBar(
        selectedUser: UserInfo,
        setSelectedUser: (UserInfo) -> Unit,
    ) {
        var selectedUser by mutableProperty(selectedUser, setSelectedUser)

        with(viewModel) {
            AnimatedVisibilityWithBlur(
                visible = showUserFilter,
                enter = fadeIn() + expandVertically(clip = false),
                exit = fadeOut() + shrinkVertically(clip = false),
            ) {
                FilterBarLayout(
                    leading = {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(8.dp)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            PointerInjector()
                            Text(stringResource(R.string.label_apps_user_select))
                        }
                    },
                    trailing = {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            var refreshFinished by rememberAutoRestorableState(false)
                            PointerInjector()
                            AnimatedContent(refreshFinished) {
                                if (it) {
                                    IconButtonSizedIcon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = stringResource(R.string.label_apps_user_list_refresh_done),
                                    )
                                } else {
                                    IconButtonWithTooltip(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = R.string.action_apps_user_list_refresh,
                                        onClick = {
                                            updateUsers()
                                            refreshFinished = true
                                        },
                                    )
                                }
                            }
                        }
                    },
                ) { contentPadding ->
                    LazyRow(
                        modifier = Modifier
                            .horizontalAlphaMaskLinear(
                                LinearMaskData(
                                    startDp = contentPadding.calculateStartPadding(
                                        LocalLayoutDirection.current
                                    ),
                                    endDp = 0.dp,
                                ),
                                LinearMaskData(
                                    startDp = contentPadding.calculateEndPadding(
                                        LocalLayoutDirection.current
                                    ),
                                    endDp = 0.dp,
                                    reverse = true,
                                ),
                                map = { CubicBezierEasing(.1f, 1f, 0f, 1f).transform(it) },
                            ),
                        contentPadding = contentPadding,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(users, { it }) {
                            val selected = selectedUser == it
                            FilterChip(
                                selected = selected,
                                onClick = { selectedUser = it },
                                label = { Text(it.name.getString()) },
                                leadingIcon = {
                                    AnimatedVisibility(
                                        visible = selected,
                                        enter = fadeIn() + expandHorizontally(),
                                        exit = fadeOut() + shrinkHorizontally(),
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    context(contentPadding: PaddingValues)
    fun NoAppsBackground(status: AppListStatus) {
        // TODO: 无障碍
        AnimatedContent(
            targetState = status,
        ) { (isEmpty, isAllFiltered) ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isEmpty) {
                    Text(
                        text = stringResource(R.string.message_apps_empty),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    if (isAllFiltered) {
                        Spacer(Modifier.height(24.dp))
                        TextButton(
                            onClick = { showFilterBottomSheet = true },
                        ) {
                            Text(stringResource(R.string.label_apps_empty_filter_show))
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FilterBottomSheet(
        sortMethod: AppSort,
        setSortMethod: (AppSort) -> Unit,
        filteredMethods: Set<AppFilter>,
        setFilteredMethods: (Set<AppFilter>) -> Unit,
    ) {
        var sortMethod by mutableProperty(sortMethod, setSortMethod)
        var filteredMethods by mutableProperty(filteredMethods, setFilteredMethods)

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
                    AppSort.entries.forEach {
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
                            filteredMethods = if (allSelected) {
                                AppFilter.usableEntries
                            } else {
                                emptySet()
                            }
                        },
                        label = { Text(stringResource(AppFilter.All.labelRes)) },
                        leadingIcon = {
                            AnimatedContent(allSelected) { allSelected ->
                                Icon(
                                    imageVector = if (allSelected) Icons.Default.Check else Icons.Default.Remove,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                    AppFilter.usableEntries.forEach {
                        val selected = !filteredMethods.contains(it)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (filteredMethods.contains(it)) {
                                    filteredMethods -= it
                                } else {
                                    filteredMethods += it
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

    @Composable
    fun FilterBarLayout(
        modifier: Modifier = Modifier,
        leading: @Composable (() -> Unit)? = null,
        trailing: @Composable (() -> Unit)? = null,
        content: @Composable (contentPadding: PaddingValues) -> Unit,
    ) {
        SubcomposeLayout(modifier) { constraints ->
            val width = constraints.maxWidth

            val contentPlaceablesForHeight = subcompose("content_height") {
                content(PaddingValues())
            }.fastMap {
                it.measure(constraints)
            }

            val height = contentPlaceablesForHeight.maxOfOrNull { it.height } ?: 0

            val childConstraints = constraints.copy(
                maxHeight = height,
            )

            val leading = subcompose("leading") { leading?.invoke() }
                .fastMap { it.measure(childConstraints) }

            val leadingWidth = leading.fastMaxBy { it.width }?.width ?: 0
            val leadingHeight = leading.fastMaxBy { it.height }?.height ?: 0
            val leadingX = when (layoutDirection) {
                LayoutDirection.Ltr -> 0
                LayoutDirection.Rtl -> width - leadingWidth
            }
            val leadingY = (height - leadingHeight) / 2

            val trailing = subcompose("trailing") { trailing?.invoke() }
                .fastMap { it.measure(childConstraints) }

            val trailingWidth = trailing.fastMaxBy { it.width }?.width ?: 0
            val trailingHeight = trailing.fastMaxBy { it.height }?.height ?: 0
            val trailingX = when (layoutDirection) {
                LayoutDirection.Ltr -> width - trailingWidth
                LayoutDirection.Rtl -> 0
            }
            val trailingY = (height - trailingHeight) / 2

            val contentPadding = PaddingValues(
                start = leadingWidth.toDp(),
                end = trailingWidth.toDp(),
            )

            val content =
                subcompose("content") { content(contentPadding) }.fastMap { it.measure(constraints) }

            layout(width, height) {
                content.fastForEach { it.place(0, 0) }
                leading.fastForEach { it.place(leadingX, leadingY) }
                trailing.fastForEach { it.place(trailingX, trailingY) }
            }
        }
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    context(
        viewModel: AppViewModel,
        sharedTransitionScope: SharedTransitionScope,
    )
    fun GroupedLazyListScope.appList(
        list: List<PackageInfo>,
        @StringRes header: Int,
    ) {
        if (list.isEmpty()) return

        card(
            colors = { CardColorsLowest },
        ) {
            header(
                text = header,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            items(
                items = list,
                key = { it.listKey },
                contentType = { "app" },
                modifier = {
                    Modifier
                        .animateItem()
                        .padding(horizontal = 16.dp)
                },
            ) { info ->
                AnimatedContent(
                    targetState = currentConfiguringPackageInfo != info,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                ) { visible ->

                    if (visible) {
                        with(sharedTransitionScope) {
                            viewModel.AppListItem(
                                packageInfo = info,
                                modifier = Modifier.sharedBounds(
                                    sharedContentState = rememberSharedContentState(
                                        AppConfigurationSharedKey
                                    ),
                                    animatedVisibilityScope = this@AnimatedContent,
                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                ),
                                onClick = {
                                    currentConfiguringPackageInfo = info
                                },
                            )
                        }
                    } else {
                        viewModel.AppListItem(
                            packageInfo = info,
                            modifier = Modifier
                                .alpha(0f)
                                .clearAndSetSemantics {},
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun BoxScope.PointerInjector() {
        Spacer(
            Modifier
                .matchParentSize()
                .pointerInteropFilter { true }
        )
    }

    data class AppListStatus(
        val isEmpty: Boolean,
        val isFiltered: Boolean,
    )

    val PackageInfo.listKey: String get() = "${packageName}-${applicationInfo?.uid}"

    context(viewModel: AppViewModel)
    fun List<PackageInfo>.filtered(filters: Set<AppFilter>) = filters.fold(this) { acc, filter ->
        with(filter) {
            acc.filtered()
        }
    }

    context(viewModel: AppViewModel)
    fun List<PackageInfo>.sorted(sort: AppSort) = with(sort) {
        sorted()
    }

    context(viewModel: AppViewModel)
    fun List<PackageInfo>.processed(sort: AppSort, filters: Set<AppFilter>) =
        filtered(filters).sorted(sort)

    fun getUserId(uid: Int): Int {
        val function = UserHandle::class.staticFunctions.first { it.name == "getUserId" }
        return function.call(uid) as Int
    }
}
