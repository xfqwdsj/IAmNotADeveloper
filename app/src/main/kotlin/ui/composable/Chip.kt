package top.ltfan.notdeveloper.ui.composable

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import top.ltfan.notdeveloper.R

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    @StringRes text: Int,
    modifier: Modifier = Modifier,
    @DrawableRes leadingPlaceholderIcon: Int? = null,
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
    @DrawableRes leadingPlaceholderIcon: Int? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Box(modifier) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = {
                Text(
                    text = text,
//                    modifier = Modifier.keepSizeWhenLookingAhead(),
                )
            },
            leadingIcon = {
                if (leadingPlaceholderIcon != null) {
                    AnimatedContentWithBlur(selected) { selected ->
                        if (selected) {
                            Icon(painterResource(R.drawable.check_24px), contentDescription = null)
                        } else {
                            Icon(painterResource(leadingPlaceholderIcon), contentDescription = null)
                        }
                    }
                } else {
                    AnimatedVisibilityWithBlur(
                        visible = selected,
                        direction = EnterExitPredefinedDirection.Horizontal,
                    ) {
                        Icon(painterResource(R.drawable.check_24px), contentDescription = null)
                    }
                }
            },
            trailingIcon = trailing,
        )
    }
}
