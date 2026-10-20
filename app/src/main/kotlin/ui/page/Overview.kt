package top.ltfan.notdeveloper.ui.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.ltfan.m3x.backdrop.BackdropHost
import top.ltfan.m3x.backdrop.captureBackdrop
import top.ltfan.m3x.backdrop.rememberBackdropLayer
import top.ltfan.m3x.compose.foundation.GroupedLazyColumn
import top.ltfan.m3x.compose.foundation.item
import top.ltfan.m3x.compose.foundation.only
import top.ltfan.m3x.compose.foundation.plus
import top.ltfan.m3x.compose.material3.ListItemColorsTransparent
import top.ltfan.m3x.material.backdrop.BackdropCenterAlignedTopAppBar
import top.ltfan.m3x.material.card
import top.ltfan.m3x.material.item
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.ui.composable.PreferenceItem
import top.ltfan.notdeveloper.ui.composable.StatusCard
import top.ltfan.notdeveloper.ui.composable.categoryCards
import top.ltfan.notdeveloper.ui.util.AppWindowInsets
import top.ltfan.notdeveloper.ui.util.appBarEffects
import top.ltfan.notdeveloper.ui.util.appBarMask
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

object Overview : Main() {
    override val navigationLabel: Int = R.string.label_nav_overview
    override val navigationIcon = R.drawable.home_24px

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun AppViewModel.Content() {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        val background = MaterialTheme.colorScheme.background
        val backdrop = rememberBackdropLayer(background)
        BackdropHost(backdrop) {
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    BackdropCenterAlignedTopAppBar(
                        mask = appBarMask(progressive),
                        effects = appBarEffects(progressive),
                        title = {
                            Text(stringResource(R.string.app_name))
                        },
                        windowInsets = AppWindowInsets.only { horizontal + top },
                        scrollBehavior = scrollBehavior,
                    )
                },
                contentWindowInsets = AppWindowInsets,
            ) { contentPadding ->
                val contentPadding = contentPadding + PaddingValues(top = 16.dp, bottom = 16.dp)

                GroupedLazyColumn(
                    modifier = Modifier.captureBackdrop(backdrop).fillMaxSize(),
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
                                    Text(
                                        stringResource(
                                            R.string.toggle_overview_use_global_preferences,
                                        ),
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        stringResource(
                                            R.string.description_overview_use_global_preferences,
                                        ),
                                    )
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
}
