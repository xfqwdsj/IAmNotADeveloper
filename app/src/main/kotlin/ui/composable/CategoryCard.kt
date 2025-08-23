package top.ltfan.notdeveloper.ui.composable

import androidx.compose.ui.Modifier
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.ui.theme.CardColorsLowest
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

context(viewModel: AppViewModel)
fun GroupedLazyListScope.categoryCards(
    groups: List<DetectionCategory>,
    afterChange: (DetectionMethod) -> Unit,
    afterTest: (DetectionMethod, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    cards(
        groups = groups,
        colors = { CardColorsLowest },
    ) {
        categoryCard(
            category = it,
            afterChange = afterChange,
            afterTest = afterTest,
            modifier = modifier,
        )
    }
}

context(viewModel: AppViewModel)
fun CardLazyGroup.categoryCard(
    category: DetectionCategory,
    afterChange: (DetectionMethod) -> Unit,
    afterTest: (DetectionMethod, Boolean) -> Unit,
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
        DetectionItem(
            method = method,
            afterChange = afterChange,
            afterTest = afterTest,
            testTrigger = viewModel.globalDetectionTestTrigger,
        )
    }
}
