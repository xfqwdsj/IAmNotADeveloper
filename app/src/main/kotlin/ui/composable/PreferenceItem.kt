package top.ltfan.notdeveloper.ui.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.ui.util.ListItemColorsTransparent

@Composable
fun PreferenceItem(
    @StringRes nameId: Int,
    testResult: Boolean,
    checked: Boolean,
    onValueChange: (Boolean) -> Unit,
    enabled: Boolean,
) {
    val description = if (testResult) {
        stringResource(R.string.test_result_on)
    } else {
        stringResource(R.string.test_result_off)
    }

    ListItem(
        headlineContent = {
            Text(stringResource(nameId))
        },
        modifier = Modifier
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onValueChange,
            )
            .semantics {
                contentDescription = description
            },
        leadingContent = {
            if (testResult) {
                Icon(
                    Icons.Outlined.Error,
                    contentDescription = null,
                )
            } else {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                )
            }
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = null, enabled = enabled)
        },
        colors = ListItemColorsTransparent,
    )
}
