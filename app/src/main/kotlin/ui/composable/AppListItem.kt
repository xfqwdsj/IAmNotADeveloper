package top.ltfan.notdeveloper.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.data.PackageInfoWrapper
import top.ltfan.notdeveloper.ui.theme.ListItemColorsTransparent
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@Composable
context(viewModel: AppViewModel)
fun AppListItem(
    packageInfo: PackageInfoWrapper,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    with(viewModel) {
        val packageInfo = remember(packageInfo) { packageInfo.info }

        val applicationInfo = remember(packageInfo) { packageInfo.applicationInfo!! }

        val appIcon = remember(packageInfo) {
            applicationInfo.loadIcon(application.packageManager)
        }

        ListItem(
            headlineContent = {
                val description = stringResource(R.string.description_apps_label)
                val labelText = remember(packageInfo) {
                    applicationInfo.loadLabel(application.packageManager).toString()
                }
                Box(Modifier.semantics { text = AnnotatedString(description) }) {
                    Text(labelText)
                }
            },
            modifier = modifier.run {
                onClick?.let { clickable(onClick = it) } ?: this
            },
            overlineContent = {
                val description = stringResource(R.string.description_apps_package_name)
                Box(Modifier.semantics { text = AnnotatedString(description) }) {
                    Text(packageInfo.packageName)
                }
            },
            supportingContent = {
                val versionName = packageInfo.versionName.toString()
                val versionCode =
                    remember(packageInfo) { PackageInfoCompat.getLongVersionCode(packageInfo) }
                val description =
                    stringResource(R.string.description_apps_version, versionName, versionCode)
                val versionText =
                    stringResource(R.string.text_apps_version, versionName, versionCode)
                Box(Modifier.semantics { contentDescription = description }) {
                    Text(versionText)
                }
            },
            leadingContent = {
                AsyncImage(
                    model = ImageRequest.Builder(application)
                        .data(appIcon)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(58.dp),
                )
            },
            colors = ListItemColorsTransparent,
        )
    }
}
