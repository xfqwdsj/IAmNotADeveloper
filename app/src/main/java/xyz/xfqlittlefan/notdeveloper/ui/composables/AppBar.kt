package xyz.xfqlittlefan.notdeveloper.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * <a href="https://m3.material.io/components/top-app-bar/overview" class="external" target="_blank">Material Design 小顶部应用栏</a>，进行了边距背景适配。
 *
 * @param title 应用栏标题。
 * @param modifier 应用到应用栏的 [Modifier]。
 * @param navigationIcon 在应用栏的 start 边显示的导航图标。通常应为 [IconButton] 或 [IconToggleButton]。
 * @param actions 在应用栏的 end 边显示的操作。通常应为 [IconButton]。在此使用的默认布局为 [Row]，所以其中的图标会横向排列。
 * @param colors [TopAppBarColors] 将用于解析此顶部应用栏在不同状态下使用的颜色。请参阅 [TopAppBarDefaults.smallTopAppBarColors]。
 * @param scrollBehavior 一个 [TopAppBarScrollBehavior]，其中包含将由此顶部应用栏应用以设置其高度和颜色的各种偏移值。Scroll Behavior 旨在与滚动内容一起使用，以在内容滚动时更改顶部应用栏的外观。请参阅 [TopAppBarScrollBehavior.nestedScrollConnection]。
 *
 * @see [androidx.compose.material3.SmallTopAppBar]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.smallTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val backgroundColor by colors.containerColor(scrollBehavior?.state?.overlappedFraction ?: 0f)
    val foregroundColors = TopAppBarDefaults.smallTopAppBarColors(
        containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent
    )
    Box(Modifier.background(backgroundColor)) {
        SmallTopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = foregroundColors,
            scrollBehavior = scrollBehavior
        )
    }
}