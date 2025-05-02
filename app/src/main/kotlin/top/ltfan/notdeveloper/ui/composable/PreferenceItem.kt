package top.ltfan.notdeveloper.ui.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import top.ltfan.notdeveloper.R

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
                    Icons.Default.Clear,
                    contentDescription = stringResource(R.string.test_result_on)
                )
            } else {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(R.string.test_result_off)
                )
            }
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = null, enabled = enabled)
        }
    )
}
