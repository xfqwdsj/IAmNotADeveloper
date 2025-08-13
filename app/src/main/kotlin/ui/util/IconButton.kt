package top.ltfan.notdeveloper.ui.util

import androidx.annotation.StringRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconButtonWithTooltip(
    imageVector: ImageVector,
    @StringRes contentDescription: Int?,
    onClick: () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            contentDescription?.let { PlainTooltip { Text(stringResource(it)) } }
        },
        state = rememberTooltipState(),
        focusable = contentDescription != null,
        enableUserInput = contentDescription != null,
    ) {
        IconButton(
            onClick = onClick,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription?.let { stringResource(it) },
            )
        }
    }
}
