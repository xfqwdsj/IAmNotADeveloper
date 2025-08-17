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
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.datastore.AppFilter
import top.ltfan.notdeveloper.datastore.AppSort
import top.ltfan.notdeveloper.ui.composable.AnimatedVisibilityWithBlur
import top.ltfan.notdeveloper.ui.composable.AppListItem
import top.ltfan.notdeveloper.ui.composable.GroupedLazyColumn
import top.ltfan.notdeveloper.ui.composable.GroupedLazyListScope
import top.ltfan.notdeveloper.ui.composable.HazeAlertDialog
import top.ltfan.notdeveloper.ui.composable.IconButtonSizedIcon
import top.ltfan.notdeveloper.ui.composable.IconButtonWithTooltip
import top.ltfan.notdeveloper.ui.composable.card
import top.ltfan.notdeveloper.ui.util.AnimatedContentDefaultTransform
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
                    appListCard(
                        list = configuredList,
                        header = R.string.label_apps_list_header_configured,
                    )

                    appListCard(
                        list = unconfiguredList,
                        header = R.string.label_apps_list_header_unconfigured,
                    )
                }

                NoAppsBackground()
                FilterBottomSheet()
            }

            AppConfiguration(
                packageInfo = currentConfiguringPackageInfo,
                dismiss = { currentConfiguringPackageInfo = null },
            )
        }

        LaunchedEffect(Unit) {
            updateAppList(queryAppList().also {
                isAppListError = it.second
            }.first)
        }

        LaunchedEffect(appList, databaseList, appSortMethod, appFilteredMethods.size) {
            val groupFilters = listOf(AppFilter.Configured, AppFilter.Unconfigured)
            val filters = appFilteredMethods.subtract(groupFilters)
            configuredList = if (AppFilter.Configured !in appFilteredMethods) {
                appList.processed(appSortMethod, filters)
            } else emptyList()
            unconfiguredList = if (AppFilter.Unconfigured !in appFilteredMethods) {
                appList.filter { it !in configuredList }.processed(appSortMethod, filters)
            } else emptyList()
        }

        LaunchedEffect(isAppListError) {
            if (isAppListError) {
                snackbarHostState.showSnackbar(snackbarMessage)
            }
        }

        DisposableEffect(currentConfiguringPackageInfo) {
            showNavBar = currentConfiguringPackageInfo == null

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
            contentDescription = R.string.action_apps_bottom_sheet_filter_show,
            onClick = { showFilterBottomSheet = true },
        )
    }

    @Composable
    context(viewModel: AppViewModel)
    fun ColumnScope.FilterBar(
        selectedUser: UserInfo,
        setSelectedUser: (UserInfo) -> Unit,
        modifier: Modifier = Modifier,
        layoutModifier: Modifier = Modifier,
    ) {
        var selectedUser by mutableProperty(selectedUser, setSelectedUser)

        with(viewModel) {
            AnimatedVisibilityWithBlur(showUserFilter, modifier) {
                FilterBarLayout(
                    modifier = layoutModifier,
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
    context(viewModel: AppViewModel, contentPadding: PaddingValues)
    fun NoAppsBackground() {
        with(viewModel) {
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    context(viewModel: AppViewModel)
    fun FilterBottomSheet() {
        with(viewModel) {
            if (showFilterBottomSheet) {
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
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppSort.entries.forEach {
                            val selected = appSortMethod == it
                            FilterChip(
                                selected = selected,
                                onClick = { appSortMethod = it },
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
                        text = stringResource(R.string.label_apps_bottom_sheet_filter),
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
                        val allSelected = appFilteredMethods.isEmpty()
                        FilterChip(
                            selected = allSelected,
                            onClick = {
                                appFilteredMethods = if (allSelected) {
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

    val AppListContainerRadius = 12.dp

    @OptIn(ExperimentalSharedTransitionApi::class)
    context(
        viewModel: AppViewModel,
        sharedTransitionScope: SharedTransitionScope,
    )
    fun GroupedLazyListScope.appListCard(
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
                    val radius by transition.animateDp(
                        label = "AppListItemRadius",
                    ) {
                        when (it) {
                            EnterExitState.PreEnter -> AppConfigurationContainerRadius
                            EnterExitState.Visible -> AppListContainerRadius
                            EnterExitState.PostExit -> AppConfigurationContainerRadius
                        }
                    }

                    if (visible) {
                        with(sharedTransitionScope) {
                            Box(
                                Modifier
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(
                                            AppConfigurationSharedKey.Container
                                        ),
                                        animatedVisibilityScope = this@AnimatedContent,
                                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                    )
                                    .clip(RoundedCornerShape(radius))
                            ) {
                                val headerText = stringResource(header)
                                viewModel.AppListItem(
                                    packageInfo = info,
                                    modifier = Modifier
                                        .sharedBounds(
                                            sharedContentState = rememberSharedContentState(
                                                AppConfigurationSharedKey.ListItem
                                            ),
                                            animatedVisibilityScope = this@AnimatedContent,
                                            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                        )
                                        .clip(RoundedCornerShape(radius))
                                        .semantics {
                                            contentDescription = headerText
                                        },
                                    onClick = {
                                        currentConfiguringPackageInfo = info
                                    },
                                )
                            }
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

    data class AppListState(
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
