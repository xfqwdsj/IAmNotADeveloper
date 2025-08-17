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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.contentOverlayHaze
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

val AppConfigurationContainerRadius = 16.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
context(
    page: Page,
    sharedTransitionScope: SharedTransitionScope,
)
fun AppViewModel.AppConfiguration(
    packageInfo: PackageInfo?,
    dismiss: () -> Unit,
) {
    val scrim = MaterialTheme.colorScheme.scrim.copy(.2f)
    val title = stringResource(R.string.title_apps_configuration_modal)
    val closeDescription = stringResource(R.string.action_apps_configuration_modal_close)
    with(sharedTransitionScope) {
        AnimatedContent(
            targetState = packageInfo,
            transitionSpec = { fadeIn() togetherWith fadeOut() using null },
        ) { packageInfo ->
            if (packageInfo != null) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(scrim)
                        .semantics { isTraversalGroup = true },
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
                    Column(
                        modifier = Modifier
                            .padding(AppWindowInsets.asPaddingValues())
                            .padding(64.dp)
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(
                                    AppConfigurationSharedKey
                                ),
                                animatedVisibilityScope = this@AnimatedContent,
                                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                            )
                            .size(200.dp)
                            .clip(RoundedCornerShape(AppConfigurationContainerRadius))
                            .contentOverlayHaze()
                            .semantics {
                                paneTitle = title
                                traversalIndex = 0f
                            },
                    ) {
                        Text("1")
                    }
                }
            }
        }
    }
}

data object AppConfigurationSharedKey
