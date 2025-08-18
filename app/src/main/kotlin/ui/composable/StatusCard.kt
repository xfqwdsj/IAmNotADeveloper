package top.ltfan.notdeveloper.ui.composable

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.focused
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.xposed.statusIsModuleActivated

@Composable
fun StatusCard(
    modifier: Modifier = Modifier,
    isModuleActivated: Boolean = statusIsModuleActivated,
    isPreferencesReady: Boolean,
    isServiceConnected: Boolean,
) {
    val status = Status.from(isModuleActivated, isPreferencesReady, isServiceConnected)

    var expanded by remember { mutableStateOf(false) }

    val collapseText = stringResource(R.string.description_more_info_collapse)

    ElevatedCard(
        onClick = { expanded = !expanded },
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                if (expanded) {
                    contentDescription = collapseText
                }
            },
        colors = CardDefaults.cardColors(containerColor = status.containerColor),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(24.dp))
            Icon(status.icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = status.summary,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 24.dp)
                        .run {
                            if (expanded) {
                                clearAndSetSemantics {}
                            } else this
                        },
                    style = MaterialTheme.typography.headlineSmall,
                )
                AnimatedVisibilityWithBlur(visible = isPreferencesReady) {
                    Text(
                        text = stringResource(R.string.description_changes_application),
                        modifier = Modifier
                            .padding(start = 16.dp, end = 24.dp)
                            .run {
                                if (expanded) {
                                    clearAndSetSemantics {}
                                } else this
                            },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                AnimatedVisibilityWithBlur(visible = status != Status.Normal) {
                    Text(
                        text = stringResource(R.string.description_more_info),
                        modifier = Modifier
                            .padding(start = 16.dp, end = 24.dp)
                            .run {
                                if (expanded) {
                                    clearAndSetSemantics {}
                                } else this
                            },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                DynamicSpacer(status != Status.Normal || expanded)
                StatusEntry(
                    expanded,
                    working = isModuleActivated,
                    workingText = R.string.status_entry_activation_y,
                    notWorkingText = R.string.status_entry_activation_n,
                    descriptionText = R.string.status_entry_activation_description,
                )
                DynamicSpacer(expanded)
                StatusEntry(
                    expanded,
                    working = isServiceConnected,
                    workingText = R.string.status_entry_service_y,
                    notWorkingText = R.string.status_entry_service_n,
                    descriptionText = R.string.status_entry_service_description,
                )
                DynamicSpacer(expanded)
                StatusEntry(
                    expanded,
                    working = isPreferencesReady,
                    workingText = R.string.status_entry_prefs_y,
                    notWorkingText = R.string.status_entry_prefs_n,
                    descriptionText = R.string.status_entry_prefs_description,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.DynamicSpacer(expanded: Boolean) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(expandFrom = Alignment.CenterVertically),
        exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically)
    ) {
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ColumnScope.StatusEntry(
    expanded: Boolean,
    working: Boolean,
    @StringRes workingText: Int,
    @StringRes notWorkingText: Int,
    @StringRes descriptionText: Int,
) {
    AnimatedVisibilityWithBlur(visible = expanded && working) {
        Text(
            text = stringResource(workingText),
            modifier = Modifier
                .padding(start = 16.dp, end = 24.dp)
                .semantics {
                    focused = true
                },
            style = MaterialTheme.typography.titleMedium,
        )
    }
    AnimatedVisibilityWithBlur(visible = !working) {
        Text(
            text = stringResource(notWorkingText),
            modifier = Modifier
                .padding(start = 16.dp, end = 24.dp)
                .semantics {
                    focused = true
                },
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
        )
    }
    AnimatedVisibilityWithBlur(visible = expanded) {
        Text(
            text = stringResource(descriptionText),
            modifier = Modifier
                .padding(start = 16.dp, end = 24.dp)
                .semantics {
                    focused = true
                },
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

enum class Status {
    Normal, Partial, Error;

    companion object {
        fun from(
            isModuleActivated: Boolean,
            isPreferencesReady: Boolean,
            isServiceConnected: Boolean,
        ): Status {
            if (!isModuleActivated) return Error
            if (!isPreferencesReady) return Partial
            if (!isServiceConnected) return Partial
            return Normal
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
