package top.ltfan.notdeveloper.ui.page

import android.content.pm.PackageInfo
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import top.ltfan.notdeveloper.ui.composable.HazeCard
import top.ltfan.notdeveloper.ui.util.HazeZIndex
import top.ltfan.notdeveloper.ui.util.contentOverlayHaze
import top.ltfan.notdeveloper.ui.util.drawPageArea
import top.ltfan.notdeveloper.ui.util.hazeEffect
import top.ltfan.notdeveloper.ui.util.hazeSource
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
context(
    page: Page,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
)
fun AppViewModel.AppConfiguration(
    packageInfo: PackageInfo,
    dismiss: () -> Unit,
) {
    with(sharedTransitionScope) {
        Column(
            modifier = Modifier
//                .padding(contentPadding)
                .padding(64.dp)
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(AppConfigurationSharedKey),
                    animatedVisibilityScope = animatedVisibilityScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                )
                .clip(CardDefaults.shape)
                .contentOverlayHaze()
                .clickable(onClick = dismiss)
                .fillMaxSize(),
        ) {

        }
    }
}

data object AppConfigurationSharedKey
