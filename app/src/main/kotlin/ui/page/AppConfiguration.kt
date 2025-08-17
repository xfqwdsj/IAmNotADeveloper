package top.ltfan.notdeveloper.ui.page

import android.content.pm.PackageInfo
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Transition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigationevent.compose.NavigationEventHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.database.PackageSettingsDao
import top.ltfan.notdeveloper.ui.composable.AppListItem
import top.ltfan.notdeveloper.ui.composable.IconButtonWithTooltip
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.contentOverlayHaze
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel
import top.ltfan.notdeveloper.util.getAppId
import top.ltfan.notdeveloper.util.getUserId

val AppConfigurationContainerRadius = 24.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
context(
    page: Page,
    transition: Transition<PackageInfo?>,
    sharedTransitionScope: SharedTransitionScope,
)
fun AppViewModel.AppConfiguration() {
    val coroutineScope = rememberCoroutineScope()

    val dao = remember(application.database) { application.database.dao() }

    fun close() {
        coroutineScope.launch {
            packageInfoConfiguringTransitionState.animateTo(null)
        }
    }

    val scrim = MaterialTheme.colorScheme.scrim.copy(.2f)
    val title = stringResource(R.string.title_apps_modal_configuration)
    val closeDescription = stringResource(R.string.action_apps_modal_configuration_close)
    with(sharedTransitionScope) {
        transition.AnimatedContent(
            transitionSpec = { fadeIn() togetherWith fadeOut() using null },
        ) { packageInfo ->
            if (packageInfo != null) {
                context(dao) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(scrim)
                            .semantics { isTraversalGroup = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Spacer(
                            Modifier
                                .matchParentSize()
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        close()
                                    }
                                }
                                .semantics(mergeDescendants = true) {
                                    traversalIndex = 1f
                                    contentDescription = closeDescription
                                    onClick {
                                        close()
                                        true
                                    }
                                }
                        )
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                            Column(
                                modifier = Modifier
                                    .padding(AppWindowInsets.asPaddingValues())
                                    .padding(24.dp)
                                    .widthIn(max = 600.dp)
                                    .fillMaxWidth()
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(
                                            AppConfigurationSharedKey.Container
                                        ),
                                        animatedVisibilityScope = this@AnimatedContent,
                                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                    )
                                    .clip(RoundedCornerShape(AppConfigurationContainerRadius))
                                    .contentOverlayHaze()
                                    .verticalScroll(rememberScrollState())
                                    .semantics {
                                        paneTitle = title
                                        traversalIndex = 0f
                                    },
                            ) {
                                Header(packageInfo, selectedUser)
                                AppListItem(
                                    packageInfo = packageInfo,
                                    modifier = Modifier.sharedBounds(
                                        sharedContentState = rememberSharedContentState(
                                            AppConfigurationSharedKey.ListItem(packageInfo)
                                        ),
                                        animatedVisibilityScope = this@AnimatedContent,
                                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                    ),
                                )
                            }
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        val packageName = packageInfo.packageName
                        val uid = packageInfo.applicationInfo!!.uid
                        val userId = getUserId(uid)
                        val appId = getAppId(uid)
                        dao.initializePackage(packageName, userId, appId)
                    }
                }

                NavigationEventHandler {
                    try {
                        it.collect { event ->
                            packageInfoConfiguringTransitionState.seekTo(event.progress, null)
                        }
                    } catch (e: CancellationException) {
                        packageInfoConfiguringTransitionState.animateTo(
                            packageInfoConfiguringTransitionState.currentState
                        )
                        return@NavigationEventHandler
                    }
                    packageInfoConfiguringTransitionState.snapTo(null)
                }
            }
        }
    }
}

@Composable
context(viewModel: AppViewModel, dao: PackageSettingsDao)
private fun Header(packageInfo: PackageInfo, userInfo: UserInfo) {
    val coroutineScope = rememberCoroutineScope()

    fun close() {
        coroutineScope.launch {
            viewModel.packageInfoConfiguringTransitionState.animateTo(null)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.title_apps_modal_configuration),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(24.dp))
        }
        Row(Modifier.padding(horizontal = 8.dp)) {
            IconButtonWithTooltip(
                imageVector = Icons.Default.ClearAll,
                contentDescription = R.string.action_apps_modal_configuration_clear,
            ) {
                val packageName = packageInfo.packageName
                val userId = userInfo.id
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    dao.deletePackageInfo(packageName, userId)
                }
                close()
            }
        }
    }
}

sealed class AppConfigurationSharedKey {
    data object Container : AppConfigurationSharedKey()
    data class ListItem(val key: Any?)
}
