package top.ltfan.notdeveloper.ui.composable

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import top.ltfan.material.m3.ExtendedFloatingActionButton
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.ui.page.Page
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

/**
 * The floating action button and its backdrop menu, with the icons and the
 * descriptions of this app. The button, the menu and the shared bounds
 * between them come from `ExtendedFloatingActionButton`.
 */
@Composable
context(viewModel: AppViewModel, page: Page)
fun ExtendedFloatingActionButtonWithMenu(
    showMenu: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    menuContent: @Composable ColumnScope.() -> Unit,
) {
    ExtendedFloatingActionButton(
        expanded = showMenu,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
        menuContentDescription = stringResource(R.string.description_fab_menu),
        buttonContent = { expanded ->
            Icon(
                painter = painterResource(
                    if (expanded) R.drawable.close_24px else R.drawable.add_24px,
                ),
                contentDescription = stringResource(
                    if (expanded) {
                        R.string.action_fab_menu_collapse
                    } else {
                        R.string.action_fab_menu_expand
                    },
                ),
            )
        },
        menuContent = menuContent,
    )
}
