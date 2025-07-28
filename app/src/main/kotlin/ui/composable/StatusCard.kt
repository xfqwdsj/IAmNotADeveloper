package top.ltfan.notdeveloper.ui.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.xposed.statusIsModuleActivated

@Composable
fun StatusCard(
    modifier: Modifier = Modifier,
    isModuleActivated: Boolean = statusIsModuleActivated,
    isPreferencesReady: Boolean
) {
    val status = Status.from(isModuleActivated, isPreferencesReady)

    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        onClick = { expanded = !expanded },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = status.containerColor),
    ) {
        Row(
            Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(status.icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(status.summary, style = MaterialTheme.typography.headlineSmall)
                AnimatedVisibility(visible = isPreferencesReady) {
                    Text(stringResource(R.string.description_changes_application))
                }
                AnimatedVisibility(visible = status != Status.Normal) {
                    Text(stringResource(R.string.description_more_info))
                }

                AnimatedVisibility(
                    visible = status != Status.Normal || expanded,
                    enter = expandVertically(expandFrom = Alignment.CenterVertically),
                    exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically)
                ) {
                    Spacer(Modifier.height(8.dp))
                }
                AnimatedVisibility(visible = expanded && isModuleActivated) {
                    Text(stringResource(R.string.status_entry_activation_y))
                }
                AnimatedVisibility(visible = !isModuleActivated) {
                    Text(
                        stringResource(R.string.status_entry_activation_n),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                AnimatedVisibility(visible = expanded) {
                    Text(
                        stringResource(R.string.status_entry_activation_description),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(expandFrom = Alignment.CenterVertically),
                    exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically)
                ) {
                    Spacer(Modifier.height(8.dp))
                }

                AnimatedVisibility(visible = expanded && isPreferencesReady) {
                    Text(stringResource(R.string.status_entry_prefs_y))
                }
                AnimatedVisibility(visible = !isPreferencesReady) {
                    Text(
                        stringResource(R.string.status_entry_prefs_n),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                AnimatedVisibility(visible = expanded) {
                    Text(
                        stringResource(R.string.status_entry_prefs_description),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Preview(device = "id:pixel_5", locale = "zh-rCN", showSystemUi = false, showBackground = false)
@Composable
fun StatusCardPreview() {
    Column {
        StatusCard(isModuleActivated = true, isPreferencesReady = true)
        StatusCard(isModuleActivated = true, isPreferencesReady = false)
        StatusCard(isModuleActivated = false, isPreferencesReady = true)
        StatusCard(isModuleActivated = false, isPreferencesReady = false)
    }
}

enum class Status {
    Normal, Partial, Error;

    companion object {
        fun from(isModuleActivated: Boolean, isPreferencesReady: Boolean): Status {
            if (!isModuleActivated) return Error
            return if (isPreferencesReady) {
                Normal
            } else Partial
        }
    }

    val containerColor: Color
        @Composable get() = when (this) {
            Normal -> MaterialTheme.colorScheme.primaryContainer
            Partial -> MaterialTheme.colorScheme.tertiaryContainer
            Error -> MaterialTheme.colorScheme.errorContainer
        }

    val icon: ImageVector
        @Composable get() = when (this) {
            Normal -> Icons.Default.CheckCircle
            else -> Icons.Default.Warning
        }

    val summary: String
        @Composable get() = stringResource(
            when (this) {
                Normal -> R.string.status_normal
                Partial -> R.string.status_partial
                Error -> R.string.status_error
            }
        )
}
