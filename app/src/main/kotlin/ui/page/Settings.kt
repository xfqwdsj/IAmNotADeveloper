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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.ltfan.material.m3.card
import top.ltfan.material.m3.core.layout.GroupedLazyColumn
import top.ltfan.material.m3.core.visual.BackdropEdge
import top.ltfan.material.m3.core.visual.LocalBackdrop
import top.ltfan.material.m3.core.visual.backdropSurface
import top.ltfan.material.m3.core.visual.captureBackdrop
import top.ltfan.material.m3.core.visual.progressiveBlur
import top.ltfan.material.m3.core.visual.rememberBackdropLayer
import top.ltfan.material.m3.settingspage.SettingsItem
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.settings.UiSettingsModelDescriber
import top.ltfan.notdeveloper.ui.composable.PreferenceItem
import top.ltfan.notdeveloper.ui.theme.LargeTopAppBarColorsTransparent
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.only
import top.ltfan.notdeveloper.ui.util.operate
import top.ltfan.notdeveloper.ui.util.plus
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
        val backdrop = rememberBackdropLayer(background)
        val coroutineScope = rememberCoroutineScope()
        val describer = remember {
            UiSettingsModelDescriber(
                dataSource = uiSettingsModelFlow,
                updateData = { model ->
                    updateUiSettingsModel(model)
                    model
                },
                coroutineScope = coroutineScope,
            )
        }
        CompositionLocalProvider(LocalBackdrop provides backdrop) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(navigationLabel)) },
                        modifier = Modifier.backdropSurface(
                            handle = backdrop,
                            shape = RectangleShape,
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
                        .captureBackdrop(backdrop)
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
                        describer.items.forEach { settingsItem ->
                            when (settingsItem) {
                                is SettingsItem.Item.Switch -> item {
                                    PreferenceItem(
                                        value = settingsItem.value,
                                        onValueChange = { settingsItem.value = it },
                                        headlineContent = { Text(settingsItem.label) },
                                        supportingContent = settingsItem.description?.let { description ->
                                            @Composable { Text(description) }
                                        },
                                    )
                                }

                                is SettingsItem.Item.Selector<*> -> {
                                    @Suppress("UNCHECKED_CAST")
                                    val selector = settingsItem as SettingsItem.Item.Selector<Any?>
                                    item {
                                        Text(
                                            text = selector.label,
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                        )
                                    }
                                    selector.options.forEach { option ->
                                        item {
                                            SelectorItem(
                                                selected = option.value == selector.value.value,
                                                label = { Text(option.label) },
                                                onSelect = { selector.value = option },
                                            )
                                        }
                                    }
                                }

                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SelectorItem(
        selected: Boolean,
        label: @Composable () -> Unit,
        onSelect: () -> Unit,
    ) {
        ListItem(
            headlineContent = label,
            trailingContent = {
                RadioButton(selected = selected, onClick = onSelect)
            },
            modifier = Modifier.clickable(onClick = onSelect),
        )
    }
}
