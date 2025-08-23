package top.ltfan.notdeveloper.ui.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import top.ltfan.notdeveloper.ui.util.keepSizeWhenLookingAhead

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    @StringRes text: Int,
    modifier: Modifier = Modifier,
    leadingPlaceholderIcon: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        text = stringResource(text),
        modifier = modifier,
        leadingPlaceholderIcon = leadingPlaceholderIcon,
        trailing = trailing,
    )
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    leadingPlaceholderIcon: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Box(modifier) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = {
                Text(
                    text = text,
                    modifier = Modifier.keepSizeWhenLookingAhead(),
                )
            },
            leadingIcon = {
                if (leadingPlaceholderIcon != null) {
                    AnimatedContentWithBlur(selected) { selected ->
                        if (selected) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        } else {
                            Icon(leadingPlaceholderIcon, contentDescription = null)
                        }
                    }
                } else {
                    AnimatedVisibilityWithBlur(
                        visible = selected,
                        direction = EnterExitPredefinedDirection.Horizontal,
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            },
            trailingIcon = trailing,
        )
    }
}
