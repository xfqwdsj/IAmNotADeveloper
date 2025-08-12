package top.ltfan.notdeveloper.ui.composable

import android.content.pm.PackageInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.application
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import top.ltfan.notdeveloper.ui.util.ListItemColorsTransparent
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@Composable
fun AppViewModel.AppListItem(
    packageInfo: PackageInfo,
    onClick: (() -> Unit)? = null,
) {
    val applicationInfo = remember(packageInfo) {
        application.packageManager.getApplicationInfo(packageInfo.packageName, 0)
    }

    val appIcon = remember(packageInfo) {
        applicationInfo.loadIcon(application.packageManager)
    }

    ListItem(
        headlineContent = {
            Text(applicationInfo.loadLabel(application.packageManager).toString())
        },
        modifier = Modifier.run {
            onClick?.let { clickable(onClick = it) } ?: this
        },
        overlineContent = {
            Text(packageInfo.packageName)
        },
        supportingContent = {
            Text("${packageInfo.versionName} (${PackageInfoCompat.getLongVersionCode(packageInfo)})")
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
