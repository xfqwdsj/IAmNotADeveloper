package top.ltfan.notdeveloper.ui.composable

import android.annotation.SuppressLint
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.ui.util.CardColorsLowest

fun GroupedLazyListScope.categoryCards(
    groups: List<DetectionCategory>,
    testResults: SnapshotStateMap<DetectionMethod, Boolean>,
    afterChange: (DetectionMethod) -> Unit,
    isPreferencesReady: Boolean,
    modifier: Modifier = Modifier,
) {
    cards(
        groups = groups,
        colors = { CardColorsLowest },
    ) {
        categoryCard(
            category = it,
            testResults = testResults,
            afterChange = afterChange,
            isPreferencesReady = isPreferencesReady,
            modifier = modifier,
        )
    }
}

fun CardLazyGroup.categoryCard(
    category: DetectionCategory,
    testResults: SnapshotStateMap<DetectionMethod, Boolean>,
    afterChange: (DetectionMethod) -> Unit,
    isPreferencesReady: Boolean,
    modifier: Modifier = Modifier,
) {
    header(
        text = category.labelResId,
        modifier = modifier,
    )

    items(
        items = category.methods,
        key = { it.toString() },
        contentType = { "detection-method" },
        modifier = { modifier },
    ) { method ->
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
            onValueChange = { pref = it },
            enabled = isPreferencesReady
        )
    }
}
