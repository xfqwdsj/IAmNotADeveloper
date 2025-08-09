package top.ltfan.notdeveloper.ui.composable

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod

@Composable
fun CategoryCard(
    category: DetectionCategory,
    testResults: SnapshotStateMap<DetectionMethod, Boolean>,
    afterChange: (DetectionMethod) -> Unit,
    isPreferencesReady: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Column {
            Text(
                text = stringResource(category.labelResId),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(Modifier.height(12.dp))

            category.methods.forEach { method ->
                @Suppress("DEPRECATION")
                @SuppressLint("WorldReadableFiles")
                var pref by rememberBooleanSharedPreference(
                    mode = android.content.Context.MODE_WORLD_READABLE,
                    key = method.name,
                    defaultValue = true,
                    afterSet = { afterChange(method) }
                )

                val testResult = testResults[method] ?: false

                PreferenceItem(
                    nameId = method.labelResId,
                    testResult = testResult,
                    checked = pref,
                    onClick = { pref = !pref },
                    enabled = isPreferencesReady
                )
            }
        }
    }
}
