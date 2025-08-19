package top.ltfan.notdeveloper.ui.composable

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.ui.theme.ListItemColorsTransparent

@Composable
fun DetectionItem(
    @StringRes nameId: Int,
    testResult: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
) {
    val description = if (testResult) {
        stringResource(R.string.test_result_on)
    } else {
        stringResource(R.string.test_result_off)
    }

    PreferenceItem(
        value = checked,
        onValueChange = onCheckedChange,
        headlineContent = {
            Text(stringResource(nameId))
        },
        modifier = Modifier.semantics {
            contentDescription = description
        },
        enabled = enabled,
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
        colors = ListItemColorsTransparent,
    )
}
