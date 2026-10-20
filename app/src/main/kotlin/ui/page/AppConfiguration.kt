package top.ltfan.notdeveloper.ui.page

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Transition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.ltfan.m3x.material.overlay.IconButtonWithTooltip
import top.ltfan.m3x.material.overlay.OverlayDialog
import top.ltfan.m3x.material.overlay.OverlayDialogProperties
import top.ltfan.m3x.material.overlay.overlaySharedBounds
import top.ltfan.m3x.overlay.OverlayNavigationTransition
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.data.PackageInfoWrapper
import top.ltfan.notdeveloper.database.PackageSettingsDao
import top.ltfan.notdeveloper.datastore.AppFilter
import top.ltfan.notdeveloper.ui.composable.AppListItem
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel
import top.ltfan.notdeveloper.util.getAppId
import top.ltfan.notdeveloper.util.getUserId

@Composable
context(transition: Transition<PackageInfoWrapper?>, sharedTransitionScope: SharedTransitionScope)
fun AppViewModel.AppConfiguration(navigationTransition: OverlayNavigationTransition) {
    // Keep the outgoing package mounted until the caller-owned timeline has completed.
    val packageInfo = transition.currentState ?: transition.targetState ?: return
    val dao = remember(application.database) { application.database.dao() }
    val title = stringResource(R.string.title_apps_modal_configuration)
    val shape = MaterialTheme.shapes.extraLarge
    with(sharedTransitionScope) {
        OverlayDialog(
            onDismissRequest = { currentConfiguringPackageInfo = null },
            visible = { currentConfiguringPackageInfo == packageInfo },
            sharedContentState =
                rememberSharedContentState(AppConfigurationSharedKey.Container(packageInfo)),
            navigationTransition = navigationTransition,
            sharedClipShape = shape,
            containerColor = MaterialTheme.colorScheme.surface,
            properties = OverlayDialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.padding(24.dp).widthIn(max = 600.dp).fillMaxWidth(),
        ) {
            context(dao) {
                Column(
                    Modifier.verticalScroll(rememberScrollState()).semantics { paneTitle = title },
                ) {
                    Header(packageInfo)
                    AppListItem(
                        packageInfo = packageInfo,
                        modifier =
                            Modifier.overlaySharedBounds(
                                rememberSharedContentState(
                                    AppConfigurationSharedKey.ListItem(packageInfo),
                                ),
                            ),
                    )
                }
            }
            LaunchedEffect(packageInfo) {
                appFilteredMethods -= AppFilter.Configured
                withContext(Dispatchers.IO) {
                    dao.initializePackage(
                        packageInfo.info.packageName,
                        packageInfo.info.getUserId(),
                        packageInfo.info.getAppId(),
                    )
                }
            }
        }
    }
}

@Composable
context(viewModel: AppViewModel, dao: PackageSettingsDao)
private fun Header(packageInfo: PackageInfoWrapper) {

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
                painter = painterResource(R.drawable.clear_all_24px),
                contentDescription = stringResource(R.string.action_apps_modal_configuration_clear),
            ) {
                val packageName = packageInfo.info.packageName
                val userId = packageInfo.info.getUserId()
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    dao.deletePackageInfo(packageName, userId)
                }
                viewModel.currentConfiguringPackageInfo = null
            }
        }
    }
}

sealed class AppConfigurationSharedKey {
    data class Container(val key: Any?) : AppConfigurationSharedKey()

    data class ListItem(val key: Any?) : AppConfigurationSharedKey()
}
