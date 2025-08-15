package top.ltfan.notdeveloper.ui.page

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.UserHandle
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.ui.composable.AppListItem
import top.ltfan.notdeveloper.ui.composable.GroupedLazyColumn
import top.ltfan.notdeveloper.ui.composable.HazeAlertDialog
import top.ltfan.notdeveloper.ui.composable.IconButtonWithTooltip
import top.ltfan.notdeveloper.ui.composable.card
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.CardColorsLowest
import top.ltfan.notdeveloper.ui.util.HazeZIndex
import top.ltfan.notdeveloper.ui.util.LinearMaskData
import top.ltfan.notdeveloper.ui.util.TopAppBarColorsTransparent
import top.ltfan.notdeveloper.ui.util.appBarHazeEffect
import top.ltfan.notdeveloper.ui.util.contentHazeSource
import top.ltfan.notdeveloper.ui.util.hazeSource
import top.ltfan.notdeveloper.ui.util.horizontalAlphaMaskLinear
import top.ltfan.notdeveloper.ui.util.only
import top.ltfan.notdeveloper.ui.util.operate
import top.ltfan.notdeveloper.ui.util.plus
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel
import kotlin.reflect.full.staticFunctions

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

    var fullList by mutableStateOf(listOf<FlaggedPackageInfo>())
    var configuredList by mutableStateOf(listOf<PackageInfo>())
    var unconfiguredList by mutableStateOf(listOf<PackageInfo>())

    var user by mutableStateOf<UserInfo?>(null)
    var sortMethod by mutableStateOf(Sort.Label)
    var filteredMethods = mutableStateSetOf<Filter>()

    var isAppListError by mutableStateOf(false)
    var showAppListErrorInfoDialog by mutableStateOf(false)

    var showFilterBottomSheet by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    context(contentPadding: PaddingValues)
    override fun AppViewModel.Content() {
        val myPackageInfo =
            remember { application.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0) }
        Scaffold(
            topBar = {
                Column(
                    Modifier
                        .hazeSource(zIndex = HazeZIndex.topBar, key = "123123")
                        .appBarHazeEffect(),
                ) {
                    TopAppBar(
                        title = {
                            Text(stringResource(navigationLabel))
                        },
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
                    )
                    FilterBar(
                        leading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .pointerInteropFilter { true }
                                    .padding(8.dp)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(stringResource(R.string.label_apps_user_select))
                            }
                        },
                        trailing = {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .pointerInteropFilter { true }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                IconButtonWithTooltip(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = R.string.action_apps_user_list_refresh,
                                    onClick = { updateUsers() },
                                )
                            }
                        },
                    ) { contentPadding ->
                        val userZeroName = stringResource(R.string.label_user_zero)
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
                            contentPadding = contentPadding.operate {
                                start += 8.dp
                                end += 8.dp
                            },
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val users = users.ifEmpty {
                                listOf(
                                    UserInfo(
                                        id = 0,
                                        name = userZeroName,
                                        flags = ApplicationInfo.FLAG_INSTALLED,
                                    ).also { user = it }
                                ) + buildList {
                                    for (i in 0..10) {
                                        add(
                                            UserInfo(
                                                id = i + 1,
                                                name = "User ${i + 1}",
                                                flags = ApplicationInfo.FLAG_INSTALLED,
                                            )
                                        )
                                    }
                                }
                            }
                            items(users, { it }) {
                                val selected = user == it
                                FilterChip(
                                    selected = selected,
                                    onClick = { user = it },
                                    label = { Text(it.name.toString()) },
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
            },
            contentWindowInsets = AppWindowInsets + contentPadding,
        ) { contentPadding ->
            val contentPadding = contentPadding.operate {
                top += 16.dp
                bottom += 16.dp
            }

            val configuredList = application.database.dao().getPackageInfoFlow()
                .collectAsStateWithLifecycle(listOf())

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    fullList = (service?.queryApps()?.ifEmpty { null }?.also {
                        isAppListError = false
                    } ?: listOf(myPackageInfo).also {
                        isAppListError = true
                    }).map { it.flagged() }
                }
            }

            LaunchedEffect(fullList, sortMethod, filteredMethods.size) {
                val groupFilters = listOf(Filter.Configured, Filter.Unconfigured)
                val filters = filteredMethods.subtract(groupFilters)
                val fullList = fullList.toMutableList()
                fullList.forEach {

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
                        items = fullList,
                        key = { it.listKey },
                        contentType = { "app" },
                        modifier = {
                            Modifier
                                .animateItem()
                                .padding(horizontal = 16.dp)
                        },
                    ) {
                        AppListItem(it.info)
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

    @Composable
    context(viewModel: AppViewModel)
    fun FilterBar(
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

    val FlaggedPackageInfo.listKey: String get() = "${packageName}-${applicationInfo?.uid}"

    enum class Sort(@param:StringRes val labelRes: Int) {
        Label(R.string.item_apps_filter_sort_label) {
            override operator fun invoke(viewModel: AppViewModel) = compareBy<FlaggedPackageInfo> {
                val packageManager = viewModel.application.packageManager
                val applicationInfo = packageManager.getApplicationInfo(it.packageName, 0)
                applicationInfo.loadLabel(packageManager).toString()
            }

            context(viewModel: AppViewModel)
            override fun List<FlaggedPackageInfo>.sorted() = sortedWith(
                invoke(viewModel) then Package(viewModel) then Updated(viewModel)
            )
        },
        Package(R.string.item_apps_filter_sort_package) {
            override operator fun invoke(viewModel: AppViewModel) =
                compareBy<FlaggedPackageInfo> { it.packageName }

            context(viewModel: AppViewModel)
            override fun List<FlaggedPackageInfo>.sorted() = sortedWith(
                invoke(viewModel) then Label(viewModel) then Updated(viewModel)
            )
        },
        Updated(R.string.item_apps_filter_sort_updated) {
            override operator fun invoke(viewModel: AppViewModel) =
                compareByDescending<FlaggedPackageInfo> { it.lastUpdateTime }

            context(viewModel: AppViewModel)
            override fun List<FlaggedPackageInfo>.sorted() = sortedWith(
                invoke(viewModel) then Package(viewModel) then Label(viewModel)
            )
        };

        abstract operator fun invoke(viewModel: AppViewModel): Comparator<FlaggedPackageInfo>

        context(viewModel: AppViewModel)
        abstract fun List<FlaggedPackageInfo>.sorted(): List<FlaggedPackageInfo>
    }

    enum class Filter(@param:StringRes val labelRes: Int) {
        All(R.string.item_apps_filter_all) {
            context(viewModel: AppViewModel)
            override fun List<FlaggedPackageInfo>.filtered(): List<FlaggedPackageInfo> = emptyList()
        },
        Configured(R.string.item_apps_filter_configured) {
            context(viewModel: AppViewModel)
            override fun List<FlaggedPackageInfo>.filtered() = filter { !it.isConfigured }
        },
        Unconfigured(R.string.item_apps_filter_unconfigured) {
            context(viewModel: AppViewModel)
            override fun List<FlaggedPackageInfo>.filtered() = filter { it.isConfigured }
        },
        System(R.string.item_apps_filter_system) {
            context(viewModel: AppViewModel)
            override fun List<FlaggedPackageInfo>.filtered() = filter { !it.isSystem }
        };

        context(viewModel: AppViewModel)
        abstract fun List<FlaggedPackageInfo>.filtered(): List<FlaggedPackageInfo>

        companion object {
            val usableEntries = Filter.entries.drop(1)
        }
    }

    data class FlaggedPackageInfo(
        val isConfigured: Boolean,
        val isSystem: Boolean,
        val info: PackageInfo,
    ) {
        val applicationInfo = info.applicationInfo
        val packageName = info.packageName
        val lastUpdateTime = info.lastUpdateTime
    }

    context(viewModel: AppViewModel)
    suspend fun PackageInfo.flagged(): FlaggedPackageInfo {
        val packageManager = viewModel.application.packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val getUserId = UserHandle::class.staticFunctions.first { it.name == "getUserId" }
        val userId = getUserId.call(applicationInfo.uid) as Int
        val dao = viewModel.application.database.dao()
        val isConfigured = dao.isPackageExists(packageName, userId)
        val isSystem =
            applicationInfo.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        return FlaggedPackageInfo(isConfigured, isSystem, this)
    }
}
