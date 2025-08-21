package top.ltfan.notdeveloper.ui.page

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import com.kyant.capsule.G2RoundedCornerShape
import kotlinx.coroutines.launch
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.data.PackageInfoWrapper
import top.ltfan.notdeveloper.datastore.AppFilter
import top.ltfan.notdeveloper.datastore.AppSort
import top.ltfan.notdeveloper.ui.composable.AnimatedVisibilityWithBlur
import top.ltfan.notdeveloper.ui.composable.AppListItem
import top.ltfan.notdeveloper.ui.composable.FilterChip
import top.ltfan.notdeveloper.ui.composable.GroupedLazyColumn
import top.ltfan.notdeveloper.ui.composable.GroupedLazyListScope
import top.ltfan.notdeveloper.ui.composable.HazeAlertDialog
import top.ltfan.notdeveloper.ui.composable.HazeFloatingActionButtonWithMenu
import top.ltfan.notdeveloper.ui.composable.HazeSnackbarHost
import top.ltfan.notdeveloper.ui.composable.IconButtonSizedIcon
import top.ltfan.notdeveloper.ui.composable.IconButtonWithTooltip
import top.ltfan.notdeveloper.ui.composable.card
import top.ltfan.notdeveloper.ui.theme.AppRadiusExtraLarge
import top.ltfan.notdeveloper.ui.theme.AppRadiusMedium
import top.ltfan.notdeveloper.ui.theme.CardColorsLowest
import top.ltfan.notdeveloper.ui.theme.TopAppBarColorsTransparent
import top.ltfan.notdeveloper.ui.util.AnimatedContentDefaultTransform
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.EmptyContentTransform
import top.ltfan.notdeveloper.ui.util.FocusRequestingEffect
import top.ltfan.notdeveloper.ui.util.LinearMaskData
import top.ltfan.notdeveloper.ui.util.appBarHaze
import top.ltfan.notdeveloper.ui.util.contentHazeSource
import top.ltfan.notdeveloper.ui.util.horizontalAlphaMaskLinear
import top.ltfan.notdeveloper.ui.util.only
import top.ltfan.notdeveloper.ui.util.operate
import top.ltfan.notdeveloper.ui.util.plus
import top.ltfan.notdeveloper.ui.util.rememberAutoRestorableState
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

object Apps : Main() {
    override val navigationLabel = R.string.label_nav_apps
    override val navigationIcon = Icons.Default.Apps

    val lazyListState = LazyListState()

    var showUserFilter by mutableStateOf(false)
    var showFabMenu by mutableStateOf(false)
    var showAppListErrorInfoDialog by mutableStateOf(false)
    var showFilterBottomSheet by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    @Composable
    context(contentPadding: PaddingValues)
    override fun AppViewModel.Content() {
        val transition = rememberTransition(packageInfoConfiguringTransitionState)

        val snackbarHostState = remember { SnackbarHostState() }
        val snackbarMessage = stringResource(R.string.message_apps_snackbar_query_failed)

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
                        FilterBar()
                    }
                },
                snackbarHost = { HazeSnackbarHost(snackbarHostState) },
                floatingActionButton = { Fab() },
                contentWindowInsets = AppWindowInsets + contentPadding,
            ) { contentPadding ->
                val contentPadding = contentPadding.operate {
                    top += 16.dp
                    bottom += 16.dp
                }

                val (configuredList, unconfiguredList) = collectAppLists()

                PullToRefreshBox(
                    isRefreshing = isAppListUpdating,
                    onRefresh = ::updateAppList,
                ) {
                    GroupedLazyColumn(
                        modifier = Modifier
                            .contentHazeSource()
                            .fillMaxSize(),
                        state = lazyListState,
                        contentPadding = contentPadding,
                        spacing = 16.dp,
                    ) {
                        context(transition) {
                            appListCard(
                                list = configuredList,
                                header = R.string.label_apps_list_header_configured,
                            )

                            appListCard(
                                list = unconfiguredList,
                                header = R.string.label_apps_list_header_unconfigured,
                            )
                        }
                    }
                }

                NoAppsBackground(contentPadding)
                FilterBottomSheet()
            }

            context(transition) { AppConfiguration() }
        }

        val event by appListErrorSnackbarTrigger.collectAsState(null)

        LaunchedEffect(event) {
            if (event != null) {
                snackbarHostState.showSnackbar(snackbarMessage)
                appListErrorSnackbarTrigger.emit(null)
            }
        }
    }

    @Composable
    context(viewModel: AppViewModel)
    fun AppBarActions() {
        if (viewModel.isAppListError) {
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
                        Text(stringResource(R.string.title_apps_dialog_query_failed))
                    },
                    text = {
                        Text(stringResource(R.string.message_apps_dialog_query_failed))
                    },
                )
            }
        }

        AnimatedContent(
            targetState = showUserFilter,
            transitionSpec = { EmptyContentTransform }
        ) { showing ->
            val rotation by transition.animateFloat(
                label = "UserFilterRotation",
                transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
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
            contentDescription = R.string.action_apps_bottom_sheet_filter_show,
            onClick = { showFilterBottomSheet = true },
        )
    }

    @Composable
    context(viewModel: AppViewModel)
    fun ColumnScope.FilterBar(
        modifier: Modifier = Modifier,
        layoutModifier: Modifier = Modifier,
    ) {
        with(viewModel) {
            AnimatedVisibilityWithBlur(showUserFilter, modifier) {
                FilterBarLayout(
                    modifier = layoutModifier.semantics {
                        isTraversalGroup = true
                    },
                    leading = {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .semantics(mergeDescendants = true) {
                                    heading()
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            PointerInjector()
                            IconButtonSizedIcon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = stringResource(R.string.label_apps_user_select),
                            )
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
                    val focusRequester = remember { FocusRequester() }

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
                            )
                            .semantics {
                                collectionInfo = CollectionInfo(
                                    rowCount = -1,
                                    columnCount = users.size,
                                )
                            },
                        contentPadding = contentPadding,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(users.size, { it }) { index ->
                            val user = users[index]
                            val selected = selectedUser == user

                            FilterChip(
                                selected = selected,
                                onClick = { selectedUser = user },
                                text = user.name.getString(),
                                modifier = Modifier
                                    .run {
                                        if (index == 0) {
                                            focusRequester(focusRequester).focusable()
                                        } else this
                                    }
                                    .semantics {
                                        collectionItemInfo = CollectionItemInfo(
                                            rowIndex = 0,
                                            rowSpan = 1,
                                            columnIndex = index,
                                            columnSpan = 1,
                                        )
                                    },
                            )

                            if (index == 0) {
                                FocusRequestingEffect(focusRequester)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    context(viewModel: AppViewModel)
    fun Fab() {
        HazeFloatingActionButtonWithMenu(
            showMenu = showFabMenu,
            onClick = { showFabMenu = !showFabMenu },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.label_apps_item_menu_fab_add_app)) },
                onClick = {},
                leadingIcon = { Icon(Icons.Default.Add, null) }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.label_apps_item_menu_fab_search)) },
                onClick = {},
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )
        }
    }

    @Composable
    context(viewModel: AppViewModel)
    fun NoAppsBackground(contentPadding: PaddingValues) {
        with(viewModel) {
            val (configuredList, unconfiguredList) = collectAppLists()

            AnimatedContent(
                targetState = AppListState(
                    isEmpty = configuredList.isEmpty() && unconfiguredList.isEmpty(),
                    isFiltered = appFilteredMethods.isNotEmpty(),
                ),
                transitionSpec = { AnimatedContentDefaultTransform using null },
                contentKey = { it.isEmpty },
            ) { (isEmpty, isFiltered) ->
                if (isEmpty) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.message_apps_empty),
                            style = MaterialTheme.typography.bodyLarge,
                        )

                        AnimatedVisibilityWithBlur(isFiltered) {
                            Column {
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
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    @Composable
    context(viewModel: AppViewModel)
    fun FilterBottomSheet() {
        if (!showFilterBottomSheet) return
        with(viewModel) {
            ModalBottomSheet(
                onDismissRequest = { showFilterBottomSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                Text(
                    text = stringResource(R.string.title_apps_bottom_sheet_filter),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.label_apps_bottom_sheet_filter_sort),
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                LookaheadScope {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .animateBounds(this),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppSort.entries.forEach {
                            val selected = appSortMethod == it
                            FilterChip(
                                selected = selected,
                                onClick = { appSortMethod = it },
                                text = it.labelRes,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.label_apps_bottom_sheet_filter),
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                LookaheadScope {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .animateBounds(this),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val allSelected = appFilteredMethods.isEmpty()
                        FilterChip(
                            selected = allSelected,
                            onClick = {
                                appFilteredMethods = if (allSelected) {
                                    AppFilter.toggleableEntries
                                } else {
                                    emptySet()
                                }
                            },
                            text = AppFilter.All.labelRes,
                            leadingPlaceholderIcon = Icons.Default.Remove,
                        )
                        AppFilter.toggleableEntries.forEach {
                            val selected = !appFilteredMethods.contains(it)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    if (appFilteredMethods.contains(it)) {
                                        appFilteredMethods -= it
                                    } else {
                                        appFilteredMethods += it
                                    }
                                },
                                text = it.labelRes,
                                modifier = Modifier.animateBounds(this@LookaheadScope),
                                leadingPlaceholderIcon = Icons.Default.Remove,
                            )
                        }
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
        transition: Transition<PackageInfoWrapper?>,
        sharedTransitionScope: SharedTransitionScope,
    )
    fun GroupedLazyListScope.appListCard(
        list: List<PackageInfoWrapper>,
        @StringRes header: Int,
    ) {
        if (list.isEmpty()) return

        card(
            colors = { CardColorsLowest },
        ) {
            header(
                text = header,
                modifier = {
                    Modifier
                        .animateItem()
                        .padding(horizontal = 16.dp)
                },
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
                val coroutineScope = rememberCoroutineScope()

                transition.AnimatedContent(
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                ) { currentInfo ->
                    val radius by this.transition.animateDp(label = "AppListItemRadius") {
                        if (it == EnterExitState.Visible) AppRadiusMedium else AppRadiusExtraLarge
                    }

                    if (currentInfo != info) {
                        with(sharedTransitionScope) {
                            Box(
                                Modifier
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(
                                            AppConfigurationSharedKey.Container(info)
                                        ),
                                        animatedVisibilityScope = this@AnimatedContent,
                                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                    )
                                    .clip(G2RoundedCornerShape(radius))
                            ) {
                                val headerText = stringResource(header)
                                AppListItem(
                                    packageInfo = info,
                                    modifier = Modifier
                                        .sharedBounds(
                                            sharedContentState = rememberSharedContentState(
                                                AppConfigurationSharedKey.ListItem(info)
                                            ),
                                            animatedVisibilityScope = this@AnimatedContent,
                                            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                        )
                                        .clip(G2RoundedCornerShape(radius))
                                        .semantics {
                                            contentDescription = headerText
                                        },
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.packageInfoConfiguringTransitionState.animateTo(
                                                info
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    } else {
                        AppListItem(
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

    data class AppListState(
        val isEmpty: Boolean,
        val isFiltered: Boolean,
    )

    val PackageInfoWrapper.listKey: String get() = "${info.packageName}-${info.applicationInfo?.uid}"

    context(viewModel: AppViewModel)
    fun Sequence<PackageInfoWrapper>.filtered(filters: Set<AppFilter>) =
        filters.fold(this) { acc, filter ->
            with(filter) {
                acc.filtered()
            }
        }.toList()

    context(viewModel: AppViewModel)
    fun Collection<PackageInfoWrapper>.sorted(sort: AppSort) = with(sort) {
        sorted()
    }

    context(viewModel: AppViewModel)
    fun Sequence<PackageInfoWrapper>.processed(sort: AppSort, filters: Set<AppFilter>) =
        filtered(filters).sorted(sort)
}
