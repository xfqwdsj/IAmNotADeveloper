package top.ltfan.notdeveloper.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.approachLayout
import androidx.compose.ui.unit.Constraints

fun Modifier.keepSizeWhenLookingAhead() = approachLayout(
    isMeasurementApproachInProgress = { false },
) { measurable, constraints ->
    val placeable = measurable.measure(
        Constraints.fixed(
            width = lookaheadSize.width,
            height = lookaheadSize.height,
        )
    )

    layout(
        lookaheadSize.width, lookaheadSize.height,
    ) {
        placeable.place(0, 0)
    }
}
