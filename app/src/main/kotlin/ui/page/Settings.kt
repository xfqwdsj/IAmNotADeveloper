package top.ltfan.notdeveloper.ui.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import top.ltfan.material.m3.card
import top.ltfan.material.m3.core.layout.GroupedLazyColumn
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.datastore.UiSettings
import top.ltfan.notdeveloper.ui.composable.PreferenceItem
import top.ltfan.notdeveloper.ui.theme.LargeTopAppBarColorsTransparent
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.BackdropEdge
import top.ltfan.notdeveloper.ui.util.LocalPageBackdrop
import top.ltfan.notdeveloper.ui.util.only
import top.ltfan.notdeveloper.ui.util.operate
import top.ltfan.notdeveloper.ui.util.plus
import top.ltfan.notdeveloper.ui.util.progressiveBlur
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

object Settings : Main() {
    override val navigationLabel = R.string.label_nav_settings
    override val navigationIcon = Icons.Default.Settings

    val lazyListState = LazyListState()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    context(contentPadding: PaddingValues)
    override fun AppViewModel.Content() {
        val background = MaterialTheme.colorScheme.background
        val backdrop = rememberLayerBackdrop {
            drawRect(background)
            drawContent()
        }
        CompositionLocalProvider(LocalPageBackdrop provides backdrop) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(navigationLabel)) },
                        modifier = Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { RectangleShape },
                            effects = { progressiveBlur(48.dp.toPx(), BackdropEdge.Top) },
                            highlight = null,
                            shadow = null,
                        ),
                        windowInsets = AppWindowInsets.only { horizontal + top },
                        colors = LargeTopAppBarColorsTransparent,
                    )
                },
                contentWindowInsets = AppWindowInsets + contentPadding,
            ) { contentPadding ->
                val padding = contentPadding.operate {
                    top += 16.dp
                    bottom += 16.dp
                }

                GroupedLazyColumn(
                    modifier = Modifier
                        .layerBackdrop(backdrop)
                        .fillMaxSize(),
                    state = lazyListState,
                    contentPadding = padding,
                    spacing = 16.dp,
                ) {
                    card(
                        colors = {
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            )
                        },
                    ) {
                        item {
                            PreferenceItem(
                                value = blurSettings is UiSettings.BlurSettings.Value.Enabled,
                                onValueChange = { enabled ->
                                    blurSettings = if (enabled) {
                                        UiSettings.BlurSettings.Value.Enabled()
                                    } else {
                                        UiSettings.BlurSettings.Value.Disabled
                                    }
                                },
                                headlineContent = {
                                    Text(stringResource(R.string.label_settings_ui_blur))
                                },
                            )
                        }
                        item {
                            PreferenceItem(
                                value = smoothRoundedCorners,
                                onValueChange = { smoothRoundedCorners = it },
                                headlineContent = {
                                    Text(stringResource(R.string.label_settings_ui_smooth_corner))
                                },
                                supportingContent = {
                                    Text(stringResource(R.string.description_settings_ui_smooth_corner))
                                },
                            )
                        }
                    }

                    val enabled = blurSettings as? UiSettings.BlurSettings.Value.Enabled
                    if (enabled != null) {
                        val current = enabled.progressiveSettings.value

                        fun select(value: UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value) {
                            blurSettings = UiSettings.BlurSettings.Value.Enabled(
                                UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings(value),
                            )
                        }

                        card {
                            item {
                                Text(
                                    text = stringResource(R.string.label_settings_ui_blur_enabled_progressive),
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                            item {
                                ProgressiveItem(
                                    selected = current is UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Disabled,
                                    label = R.string.label_settings_ui_blur_enabled_progressive_disabled,
                                    onSelect = { select(UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Disabled) },
                                )
                            }
                            item {
                                ProgressiveItem(
                                    selected = current is UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Mask,
                                    label = R.string.label_settings_ui_blur_enabled_progressive_mask,
                                    onSelect = { select(UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Mask) },
                                )
                            }
                            item {
                                ProgressiveItem(
                                    selected = current is UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Scaled.Auto,
                                    label = R.string.label_settings_ui_blur_enabled_progressive_scaled_auto,
                                    onSelect = { select(UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Scaled.Auto) },
                                )
                            }
                            item {
                                ProgressiveItem(
                                    selected = current is UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Scaled.Custom,
                                    label = R.string.label_settings_ui_blur_enabled_progressive_scaled_custom,
                                    onSelect = { select(UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Scaled.Custom()) },
                                )
                            }
                            item {
                                ProgressiveItem(
                                    selected = current is UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Full,
                                    label = R.string.label_settings_ui_blur_enabled_progressive_full,
                                    onSelect = { select(UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Full) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ProgressiveItem(
        selected: Boolean,
        label: Int,
        onSelect: () -> Unit,
    ) {
        ListItem(
            headlineContent = { Text(stringResource(label)) },
            trailingContent = {
                RadioButton(selected = selected, onClick = onSelect)
            },
            modifier = Modifier.clickable(onClick = onSelect),
        )
    }
}
