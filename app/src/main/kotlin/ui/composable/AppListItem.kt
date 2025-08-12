package top.ltfan.notdeveloper.ui.composable

import android.content.pm.ApplicationInfo
import android.os.UserHandle
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.application
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.ui.util.ListItemColorsTransparent
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel
import kotlin.reflect.full.staticFunctions

@Composable
fun AppViewModel.AppListItem(
    applicationInfo: ApplicationInfo,
    onClick: (() -> Unit)? = null,
) {
    val userInfo = remember(applicationInfo) {
        val uid = applicationInfo.uid
        val getUserId = UserHandle::class.staticFunctions.find { it.name == "getUserId" }
        val userId = getUserId?.call(uid) as Int? ?: 0
        service?.queryUser(userId) ?: UserInfo(
            id = userId,
            name = null,
            flags = 0,
        )
    }

    val appIcon = remember(applicationInfo) {
        applicationInfo.loadIcon(application.packageManager)
    }

    ListItem(
        headlineContent = {
            Row {
                Text(applicationInfo.loadLabel(application.packageManager).toString())
                Text(userInfo.name.toString())
                Text(userInfo.id.toString())
            }
        },
        leadingContent = {
            AsyncImage(
                model = ImageRequest.Builder(application)
                    .data(appIcon)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
        },
        colors = ListItemColorsTransparent,
    )
}
