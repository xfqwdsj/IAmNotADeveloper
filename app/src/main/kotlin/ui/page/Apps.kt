package top.ltfan.notdeveloper.ui.page

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.createChildTransition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
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
import top.ltfan.m3x.backdrop.BackdropHost
import top.ltfan.m3x.backdrop.rememberBackdropLayer
import top.ltfan.m3x.compose.animation.AnimatedVisibilityWithBlur
import top.ltfan.m3x.compose.animation.FocusRequestingEffect
import top.ltfan.m3x.compose.foundation.GroupedLazyColumn
import top.ltfan.m3x.compose.foundation.GroupedLazyListScope
import top.ltfan.m3x.compose.foundation.only
import top.ltfan.m3x.compose.foundation.plus
import top.ltfan.m3x.compose.material3.CardColorsLowest
import top.ltfan.m3x.compose.runtime.rememberAutoRestorableState
import top.ltfan.m3x.material.FilterChip
import top.ltfan.m3x.material.TextButton
import top.ltfan.m3x.material.backdrop.BackdropCenterAlignedTopAppBar
import top.ltfan.m3x.material.backdrop.BackdropTopAppBarControl
import top.ltfan.m3x.material.card
import top.ltfan.m3x.material.header
import top.ltfan.m3x.material.items
import top.ltfan.m3x.material.overlay.IconButtonWithTooltip
import top.ltfan.m3x.material.overlay.MenuFloatingActionButton
import top.ltfan.m3x.material.overlay.OverlayAlertDialog
import top.ltfan.m3x.material.overlay.OverlayBottomSheet
import top.ltfan.m3x.material.overlay.OverlayMenuDefaults
import top.ltfan.m3x.material.overlay.OverlayMenuItem
import top.ltfan.m3x.material.overlay.OverlaySnackbarHost
import top.ltfan.m3x.material.overlay.sharedTransitionBounds
import top.ltfan.m3x.overlay.LocalSharedTransitionScope
import top.ltfan.m3x.overlay.rememberOverlayNavigationTransition
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.data.PackageInfoWrapper
import top.ltfan.notdeveloper.datastore.AppFilter
import top.ltfan.notdeveloper.datastore.AppSort
import top.ltfan.notdeveloper.ui.composable.AppListItem
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.LinearMaskData
import top.ltfan.notdeveloper.ui.util.PageBackdropContent
import top.ltfan.notdeveloper.ui.util.appBarEffects
import top.ltfan.notdeveloper.ui.util.appBarMask
import top.ltfan.notdeveloper.ui.util.horizontalAlphaMaskLinear
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

object Apps : Main() {
    override val navigationLabel = R.string.label_nav_apps
    override val navigationIcon = R.drawable.apps_24px

    val lazyListState = LazyListState()

    var showUserFilter by mutableStateOf(false)
    var showFabMenu by mutableStateOf(false)
    var showAppListErrorInfoDialog by mutableStateOf(false)
    var showFilterBottomSheet by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun AppViewModel.Content() {
        val transition = rememberTransition(packageInfoConfiguringTransitionState)
        val navigationTransition =
            rememberOverlayNavigationTransition(
                state = packageInfoConfiguringTransitionState,
                transition = transition,
                targetState = currentConfiguringPackageInfo,
                backTarget = null,
                isVisible = { value, key ->
                    key == AppConfigurationSharedKey.Container(value) && value != null
                },
            )
        val background = MaterialTheme.colorScheme.background
        val backdrop = rememberBackdropLayer(background)

        val snackbarHostState = remember { SnackbarHostState() }
        val snackbarMessage = stringResource(R.string.message_apps_snackbar_query_failed)

        BackdropHost(backdrop) {
            Scaffold(
                topBar = {
                    Column {
                        BackdropCenterAlignedTopAppBar(
                            mask = appBarMask(progressive),
                            effects = appBarEffects(progressive),
                            title = { Text(stringResource(navigationLabel)) },
                            actions = {
                                BackdropTopAppBarControl { AppBarActions() }
                            },
                            windowInsets = AppWindowInsets.only { horizontal + top },
                        )
                        FilterBar()
                    }
                },
                floatingActionButton = { Fab() },
                contentWindowInsets = AppWindowInsets,
            ) { contentPadding ->
                val (configuredList, unconfiguredList) = collectAppLists()

                with(requireNotNull(LocalSharedTransitionScope.current)) {
                    PageBackdropContent(backdrop) {
                        GroupedLazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = lazyListState,
                            contentPadding =
                                contentPadding +
                                    PaddingValues(
                                        top = 16.dp,
                                        bottom = 16.dp,
                                    ),
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
                }

                AnimatedVisibility(
                    visible = isAppListUpdating,
                    modifier = Modifier.padding(contentPadding),
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    LinearProgressIndicator()
                }

                NoAppsBackground(contentPadding)
                FilterBottomSheet()
            }

            with(requireNotNull(LocalSharedTransitionScope.current)) {
                context(transition) { AppConfiguration(navigationTransition) }
            }

            OverlaySnackbarHost(snackbarHostState)
        }

        LaunchedEffect(appListErrorSnackbarTrigger, snackbarHostState, snackbarMessage) {
            appListErrorSnackbarTrigger.collect {
                snackbarHostState.showSnackbar(snackbarMessage)
            }
        }
    }

    @Composable
    context(viewModel: AppViewModel)
    fun AppBarActions() {
        if (viewModel.isAppListError) {
            IconButtonWithTooltip(
                painter = painterResource(R.drawable.warning_24px),
                contentDescription = stringResource(R.string.action_apps_query_details_show),
                onClick = { showAppListErrorInfoDialog = true },
            )

            OverlayAlertDialog(
                visible = { showAppListErrorInfoDialog },
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

        AnimatedContent(
            targetState = showUserFilter,
            transitionSpec = { EnterTransition.None togetherWith ExitTransition.None using null },
        ) { showing ->
            val rotation by
                transition.animateFloat(
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
                    painter = painterResource(R.drawable.expand_circle_up_24px),
                    contentDescription = stringResource(R.string.action_apps_user_select_hide),
                    modifier = Modifier.rotate(rotation),
                    onClick = { showUserFilter = false },
                )
            } else {
                IconButtonWithTooltip(
                    painter = painterResource(R.drawable.expand_circle_down_24px),
                    contentDescription = stringResource(R.string.action_apps_user_select_show),
                    modifier = Modifier.rotate(rotation),
                    onClick = { showUserFilter = true },
                )
            }
        }

        IconButtonWithTooltip(
            painter = painterResource(R.drawable.filter_list_24px),
            contentDescription = stringResource(R.string.action_apps_bottom_sheet_filter_show),
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
            AnimatedVisibilityWithBlur(
                showUserFilter,
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                modifier = modifier,
                enter = expandVertically(clip = false),
                exit = shrinkVertically(clip = false),
            ) {
                FilterBarLayout(
                    modifier =
                        layoutModifier.semantics {
                            isTraversalGroup = true
                        },
                    leading = {
                        Box(
                            modifier =
                                Modifier.fillMaxHeight()
                                    .semantics(mergeDescendants = true) {
                                        heading()
                                    }
                                    .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            PointerInjector()
                            Icon(
                                painter = painterResource(R.drawable.account_circle_24px),
                                modifier = Modifier.minimumInteractiveComponentSize().size(40.dp),
                                contentDescription =
                                    stringResource(R.string.label_apps_user_select),
                            )
                        }
                    },
                    trailing = {
                        Box(
                            modifier = Modifier.fillMaxHeight().padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            var refreshFinished by rememberAutoRestorableState(false)
                            PointerInjector()
                            AnimatedContent(refreshFinished) {
                                if (it) {
                                    Icon(
                                        painter = painterResource(R.drawable.check_24px),
                                        modifier =
                                            Modifier.minimumInteractiveComponentSize().size(40.dp),
                                        contentDescription =
                                            stringResource(
                                                R.string.label_apps_user_list_refresh_done,
                                            ),
                                    )
                                } else {
                                    IconButtonWithTooltip(
                                        painter = painterResource(R.drawable.refresh_24px),
                                        contentDescription =
                                            stringResource(R.string.action_apps_user_list_refresh),
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
                    // The request belongs to the bar, not to the first item: inside the lazy item
                    // it
                    // would run again every time that item re-enters the composition while
                    // scrolling,
                    // and steal focus back to the first chip.
                    FocusRequestingEffect(focusRequester)

                    LazyRow(
                        modifier =
                            Modifier.horizontalAlphaMaskLinear(
                                    LinearMaskData(
                                        startDp =
                                            contentPadding.calculateStartPadding(
                                                LocalLayoutDirection.current,
                                            ),
                                        endDp = 0.dp,
                                    ),
                                    LinearMaskData(
                                        startDp =
                                            contentPadding.calculateEndPadding(
                                                LocalLayoutDirection.current,
                                            ),
                                        endDp = 0.dp,
                                        reverse = true,
                                    ),
                                    map = { CubicBezierEasing(.1f, 1f, 0f, 1f).transform(it) },
                                )
                                .semantics {
                                    collectionInfo =
                                        CollectionInfo(
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
                                label = { Text(user.name.getString()) },
                                leadingIcon = {
                                    if (selected) Icon(painterResource(R.drawable.check_24px), null)
                                },
                                modifier =
                                    Modifier.run {
                                            if (index == 0) {
                                                focusRequester(focusRequester).focusable()
                                            } else this
                                        }
                                        .semantics {
                                            collectionItemInfo =
                                                CollectionItemInfo(
                                                    rowIndex = 0,
                                                    rowSpan = 1,
                                                    columnIndex = index,
                                                    columnSpan = 1,
                                                )
                                        },
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    context(viewModel: AppViewModel)
    fun Fab() {
        MenuFloatingActionButton(
            expanded = { showFabMenu },
            onExpandedChange = { expanded -> showFabMenu = expanded },
            menuContentDescription = stringResource(R.string.description_fab_menu),
            buttonContent = { expanded ->
                Icon(
                    painterResource(if (expanded) R.drawable.close_24px else R.drawable.add_24px),
                    contentDescription =
                        stringResource(
                            if (expanded) R.string.action_fab_menu_collapse
                            else R.string.action_fab_menu_expand,
                        ),
                )
            },
            menuContent = {
                OverlayMenuItem(
                    text = { Text(stringResource(R.string.label_apps_item_menu_fab_add_app)) },
                    onClick = { showFabMenu = false },
                    leadingIcon = { Icon(painterResource(R.drawable.add_24px), null) },
                    shape = OverlayMenuDefaults.itemShape(0, 2),
                )
                OverlayMenuItem(
                    text = { Text(stringResource(R.string.label_apps_item_menu_fab_search)) },
                    onClick = { showFabMenu = false },
                    leadingIcon = { Icon(painterResource(R.drawable.search_24px), null) },
                    shape = OverlayMenuDefaults.itemShape(1, 2),
                )
            },
        )
    }

    @Composable
    context(viewModel: AppViewModel)
    fun NoAppsBackground(contentPadding: PaddingValues) {
        with(viewModel) {
            val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
            val (configuredList, unconfiguredList) = collectAppLists()

            AnimatedContent(
                targetState =
                    AppListState(
                        isEmpty = configuredList.isEmpty() && unconfiguredList.isEmpty(),
                        isFiltered = appFilteredMethods.isNotEmpty(),
                    ),
                transitionSpec = {
                    fadeIn(effectsSpec) togetherWith fadeOut(effectsSpec) using null
                },
                contentKey = { it.isEmpty },
            ) { (isEmpty, isFiltered) ->
                if (isEmpty) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(contentPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.message_apps_empty),
                            style = MaterialTheme.typography.bodyLarge,
                        )

                        AnimatedVisibilityWithBlur(
                            isFiltered,
                            animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                            enter = expandVertically(clip = false),
                            exit = shrinkVertically(clip = false),
                        ) {
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

    @Composable
    context(viewModel: AppViewModel)
    fun FilterBottomSheet() {
        with(viewModel) {
            OverlayBottomSheet(
                visible = { showFilterBottomSheet },
                onDismissRequest = { showFilterBottomSheet = false },
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
                with(requireNotNull(LocalSharedTransitionScope.current)) filters@{
                    FlowRow(
                        modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp).animateBounds(this),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AppSort.entries.forEach {
                            val selected = appSortMethod == it
                            FilterChip(
                                selected = selected,
                                onClick = { appSortMethod = it },
                                label = { Text(stringResource(it.labelRes)) },
                                leadingIcon = {
                                    Icon(
                                        painterResource(
                                            if (selected) R.drawable.check_24px
                                            else R.drawable.remove_24px,
                                        ),
                                        null,
                                    )
                                },
                                modifier = Modifier.skipToLookaheadSize(),
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
                with(requireNotNull(LocalSharedTransitionScope.current)) filters@{
                    FlowRow(
                        modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp).animateBounds(this),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val allSelected = appFilteredMethods.isEmpty()
                        FilterChip(
                            selected = allSelected,
                            onClick = {
                                appFilteredMethods =
                                    if (allSelected) {
                                        AppFilter.toggleableEntries
                                    } else {
                                        emptySet()
                                    }
                            },
                            label = { Text(stringResource(AppFilter.All.labelRes)) },
                            leadingIcon = {
                                Icon(
                                    painterResource(
                                        if (allSelected) R.drawable.check_24px
                                        else R.drawable.remove_24px,
                                    ),
                                    null,
                                )
                            },
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
                                label = { Text(stringResource(it.labelRes)) },
                                leadingIcon = {
                                    Icon(
                                        painterResource(
                                            if (selected) R.drawable.check_24px
                                            else R.drawable.remove_24px,
                                        ),
                                        null,
                                    )
                                },
                                modifier =
                                    Modifier.animateBounds(this@filters).skipToLookaheadSize(),
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

            val contentPlaceablesForHeight =
                subcompose("content_height") {
                        content(PaddingValues())
                    }
                    .fastMap {
                        it.measure(constraints)
                    }

            val height = contentPlaceablesForHeight.maxOfOrNull { it.height } ?: 0

            val childConstraints =
                constraints.copy(
                    maxHeight = height,
                )

            val leading =
                subcompose("leading") { leading?.invoke() }.fastMap { it.measure(childConstraints) }

            val leadingWidth = leading.fastMaxBy { it.width }?.width ?: 0
            val leadingHeight = leading.fastMaxBy { it.height }?.height ?: 0
            val leadingX =
                when (layoutDirection) {
                    LayoutDirection.Ltr -> 0
                    LayoutDirection.Rtl -> width - leadingWidth
                }
            val leadingY = (height - leadingHeight) / 2

            val trailing =
                subcompose("trailing") { trailing?.invoke() }
                    .fastMap { it.measure(childConstraints) }

            val trailingWidth = trailing.fastMaxBy { it.width }?.width ?: 0
            val trailingHeight = trailing.fastMaxBy { it.height }?.height ?: 0
            val trailingX =
                when (layoutDirection) {
                    LayoutDirection.Ltr -> width - trailingWidth
                    LayoutDirection.Rtl -> 0
                }
            val trailingY = (height - trailingHeight) / 2

            val contentPadding =
                PaddingValues(
                    start = leadingWidth.toDp(),
                    end = trailingWidth.toDp(),
                )

            val content =
                subcompose("content") { content(contentPadding) }
                    .fastMap { it.measure(constraints) }

            layout(width, height) {
                content.fastForEach { it.place(0, 0) }
                leading.fastForEach { it.place(leadingX, leadingY) }
                trailing.fastForEach { it.place(trailingX, trailingY) }
            }
        }
    }

    context(
    viewModel: AppViewModel,
    transition: Transition<PackageInfoWrapper?>,
    sharedTransitionScope: SharedTransitionScope)
    fun GroupedLazyListScope.appListCard(
        list: List<PackageInfoWrapper>,
        @StringRes header: Int,
    ) {
        if (list.isEmpty()) return

        card(
            colors = { CardColorsLowest },
        ) {
            header(
                textRes = header,
                modifier = {
                    Modifier.animateItem().padding(horizontal = 16.dp)
                },
            )

            items(
                items = list,
                key = { it.listKey },
                contentType = { "app" },
                modifier = {
                    Modifier.animateItem().padding(horizontal = 16.dp)
                },
            ) { info ->
                val itemTransition =
                    transition.createChildTransition(label = "AppConfiguration:${info.listKey}") {
                        it == info
                    }
                itemTransition.AnimatedContent(
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                ) { configuring ->
                    if (!configuring) {
                        with(sharedTransitionScope) {
                            Box(
                                Modifier.sharedTransitionBounds(
                                    sharedContentState =
                                        rememberSharedContentState(
                                            AppConfigurationSharedKey.Container(info),
                                        ),
                                    animatedVisibilityScope = this@AnimatedContent,
                                    shape = MaterialTheme.shapes.medium,
                                ),
                            ) {
                                val headerText = stringResource(header)
                                AppListItem(
                                    packageInfo = info,
                                    modifier =
                                        Modifier.sharedTransitionBounds(
                                                sharedContentState =
                                                    rememberSharedContentState(
                                                        AppConfigurationSharedKey.ListItem(info),
                                                    ),
                                                animatedVisibilityScope = this@AnimatedContent,
                                                shape = MaterialTheme.shapes.medium,
                                            )
                                            .semantics {
                                                contentDescription = headerText
                                            },
                                    onClick = {
                                        viewModel.currentConfiguringPackageInfo = info
                                    },
                                )
                            }
                        }
                    } else {
                        AppListItem(
                            packageInfo = info,
                            modifier = Modifier.alpha(0f).clearAndSetSemantics {},
                        )
                    }
                }
            }
        }
    }

    context(viewModel: AppViewModel)
    override fun secondClick() {
        viewModel.updateAppList()
    }

    @Composable
    fun BoxScope.PointerInjector() {
        Spacer(
            Modifier.matchParentSize().pointerInteropFilter { true },
        )
    }

    data class AppListState(
        val isEmpty: Boolean,
        val isFiltered: Boolean,
    )

    val PackageInfoWrapper.listKey: String
        get() = "${info.packageName}-${info.applicationInfo?.uid}"

    context(viewModel: AppViewModel)
    fun Sequence<PackageInfoWrapper>.filtered(filters: Set<AppFilter>) =
        filters
            .fold(this) { acc, filter ->
                with(filter) {
                    acc.filtered()
                }
            }
            .toList()

    context(viewModel: AppViewModel)
    fun Collection<PackageInfoWrapper>.sorted(sort: AppSort) =
        with(sort) {
            sorted()
        }

    context(viewModel: AppViewModel)
    fun Sequence<PackageInfoWrapper>.processed(sort: AppSort, filters: Set<AppFilter>) =
        filtered(filters).sorted(sort)
}
