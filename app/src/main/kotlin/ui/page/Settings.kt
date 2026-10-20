package top.ltfan.notdeveloper.ui.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.ltfan.m3x.backdrop.BackdropHost
import top.ltfan.m3x.backdrop.rememberBackdropLayer
import top.ltfan.m3x.compose.foundation.only
import top.ltfan.m3x.compose.foundation.plus
import top.ltfan.m3x.compose.material3.CardColorsLowest
import top.ltfan.m3x.material.backdrop.BackdropCenterAlignedTopAppBar
import top.ltfan.m3x.settingspage.material.CardSettings
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.PageBackdropContent
import top.ltfan.notdeveloper.ui.util.appBarEffects
import top.ltfan.notdeveloper.ui.util.appBarMask
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
        val describer = settingsDescriber
        BackdropHost(backdrop) {
            Scaffold(
                topBar = {
                    BackdropCenterAlignedTopAppBar(
                        mask = appBarMask(progressive),
                        effects = appBarEffects(progressive),
                        title = { Text(stringResource(navigationLabel)) },
                        windowInsets = AppWindowInsets.only { horizontal + top },
                    )
                },
                contentWindowInsets = AppWindowInsets,
            ) { contentPadding ->
                with(this@Settings) {
                    PageBackdropContent(backdrop) {
                        CardSettings(
                            store = describer,
                            modifier = Modifier.fillMaxSize(),
                            lazyListState = lazyListState,
                            contentPadding =
                                contentPadding +
                                    PaddingValues(
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
}
