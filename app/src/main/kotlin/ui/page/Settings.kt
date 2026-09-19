package top.ltfan.notdeveloper.ui.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import top.ltfan.material.m3.CardSettings
import top.ltfan.material.m3.core.layout.only
import top.ltfan.material.m3.core.layout.plus
import top.ltfan.material.m3.core.visual.BackdropEdge
import top.ltfan.material.m3.core.visual.LocalBackdrop
import top.ltfan.material.m3.core.visual.backdropSurface
import top.ltfan.material.m3.core.visual.captureBackdrop
import top.ltfan.material.m3.core.visual.progressiveBlur
import top.ltfan.material.m3.core.visual.rememberBackdropLayer
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.settings.UiSettingsModelDescriber
import top.ltfan.notdeveloper.ui.theme.CardColorsLowest
import top.ltfan.notdeveloper.ui.theme.LargeTopAppBarColorsTransparent
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

object Settings : Main() {
    override val navigationLabel = R.string.label_nav_settings
    override val navigationIcon = R.drawable.settings_24px

    val lazyListState = LazyListState()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
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
                contentWindowInsets = AppWindowInsets,
            ) { contentPadding ->
                with(this@Settings) {
                    CardSettings(
                        store = describer,
                        modifier = Modifier
                            .captureBackdrop(backdrop)
                            .fillMaxSize(),
                        lazyListState = lazyListState,
                        contentPadding = contentPadding + PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp,
                        ),
                        cardColors = CardColorsLowest,
                    )
                }
            }
        }
    }
}
