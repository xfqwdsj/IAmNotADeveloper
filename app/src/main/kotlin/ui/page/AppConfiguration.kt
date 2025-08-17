package top.ltfan.notdeveloper.ui.page

import android.content.pm.PackageInfo
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.ui.composable.AppListItem
import top.ltfan.notdeveloper.ui.composable.IconButtonWithTooltip
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.contentOverlayHaze
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

val AppConfigurationContainerRadius = 24.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
context(
    page: Page,
    sharedTransitionScope: SharedTransitionScope,
)
fun AppViewModel.AppConfiguration(
    packageInfo: PackageInfo?,
    userInfo: UserInfo,
    dismiss: () -> Unit,
) {
    val scrim = MaterialTheme.colorScheme.scrim.copy(.2f)
    val title = stringResource(R.string.title_apps_modal_configuration)
    val closeDescription = stringResource(R.string.action_apps_modal_configuration_close)
    with(sharedTransitionScope) {
        AnimatedContent(
            targetState = packageInfo,
            transitionSpec = { fadeIn() togetherWith fadeOut() using null },
        ) { packageInfo ->
            if (packageInfo != null) {
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
                            .pointerInput(dismiss) { detectTapGestures { dismiss() } }
                            .semantics(mergeDescendants = true) {
                                traversalIndex = 1f
                                contentDescription = closeDescription
                                onClick {
                                    dismiss()
                                    true
                                }
                            }
                    )
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                        Column(
                            modifier = Modifier
                                .padding(AppWindowInsets.asPaddingValues())
                                .padding(48.dp)
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
                            Header(packageInfo, userInfo)
                            AppListItem(
                                packageInfo = packageInfo,
                                modifier = Modifier.sharedBounds(
                                    sharedContentState = rememberSharedContentState(
                                        AppConfigurationSharedKey.ListItem
                                    ),
                                    animatedVisibilityScope = this@AnimatedContent,
                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
context(viewModel: AppViewModel)
private fun Header(packageInfo: PackageInfo, userInfo: UserInfo) {
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
                    viewModel.application.database.dao().deletePackageInfo(packageName, userId)
                }
            }
        }
    }
}

enum class AppConfigurationSharedKey {
    Container, ListItem,
}
