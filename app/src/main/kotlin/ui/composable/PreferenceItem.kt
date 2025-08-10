package top.ltfan.notdeveloper.ui.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.ui.util.TransparentListItemColors

@Composable
fun PreferenceItem(
    @StringRes nameId: Int,
    testResult: Boolean,
    checked: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    ListItem(
        headlineContent = {
            Text(stringResource(nameId))
        },
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
        leadingContent = {
            if (testResult) {
                Icon(
                    Icons.Outlined.Error,
                    contentDescription = stringResource(R.string.test_result_on)
                )
            } else {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = stringResource(R.string.test_result_off)
                )
            }
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = null, enabled = enabled)
        },
        colors = TransparentListItemColors,
    )
}
