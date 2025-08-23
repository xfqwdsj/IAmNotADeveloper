package top.ltfan.notdeveloper.ui.composable

import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp

@Composable
fun PreferenceItem(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    headlineContent: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
) {
    ListItem(
        headlineContent = headlineContent,
        modifier = modifier
            .toggleable(
                value = value,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onValueChange,
            ),
        overlineContent = overlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = {
            Switch(checked = value, onCheckedChange = null, enabled = enabled)
        },
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    )
}
