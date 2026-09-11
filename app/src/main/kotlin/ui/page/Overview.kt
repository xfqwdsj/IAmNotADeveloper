package top.ltfan.notdeveloper.ui.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import top.ltfan.material.m3.card
import top.ltfan.material.m3.core.GroupedLazyColumn
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.ui.composable.PreferenceItem
import top.ltfan.notdeveloper.ui.composable.StatusCard
import top.ltfan.notdeveloper.ui.composable.categoryCards
import top.ltfan.notdeveloper.ui.theme.LargeTopAppBarColorsTransparent
import top.ltfan.notdeveloper.ui.theme.ListItemColorsTransparent
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.BackdropEdge
import top.ltfan.notdeveloper.ui.util.only
import top.ltfan.notdeveloper.ui.util.operate
import top.ltfan.notdeveloper.ui.util.plus
import top.ltfan.notdeveloper.ui.util.progressiveBlur
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

object Overview : Main() {
    override val navigationLabel: Int = R.string.label_nav_overview
    override val navigationIcon = Icons.Default.Home

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    context(contentPadding: PaddingValues)
    override fun AppViewModel.Content() {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        val background = MaterialTheme.colorScheme.background
        val backdrop = rememberLayerBackdrop {
            drawRect(background)
            drawContent()
        }
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(stringResource(R.string.app_name))
                    },
                    modifier = Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { RectangleShape },
                        effects = { progressiveBlur(48.dp.toPx(), BackdropEdge.Top) },
                        highlight = null,
                        shadow = null,
                    ),
                    windowInsets = AppWindowInsets.only { horizontal + top },
                    scrollBehavior = scrollBehavior,
                    colors = LargeTopAppBarColorsTransparent,
                )
            },
            contentWindowInsets = AppWindowInsets + contentPadding,
        ) { contentPadding ->
            val contentPadding = contentPadding.operate {
                top += 16.dp
                bottom += 16.dp
            }

            GroupedLazyColumn(
                modifier = Modifier
                    .layerBackdrop(backdrop)
                    .fillMaxSize(),
                contentPadding = contentPadding,
                spacing = 16.dp,
            ) {
                item {
                    StatusCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        isPreferencesReady = isPreferencesReady,
                        isServiceConnected = service != null,
                    )
                }

                card(
                    colors = {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        )
                    },
                    elevation = { CardDefaults.elevatedCardElevation() },
                ) {
                    item(
                        key = "global-preferences",
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) {
                        PreferenceItem(
                            value = useGlobalPreferences,
                            onValueChange = { useGlobalPreferences = it },
                            headlineContent = {
                                Text(stringResource(R.string.toggle_overview_use_global_preferences))
                            },
                            supportingContent = {
                                Text(stringResource(R.string.description_overview_use_global_preferences))
                            },
                            colors = ListItemColorsTransparent,
                        )
                    }
                }

                categoryCards(
                    groups = DetectionCategory.values,
                    afterChange = ::afterGlobalDetectionChange,
                    afterTest = ::afterGlobalDetectionTest,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
