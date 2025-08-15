package top.ltfan.notdeveloper.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isFinite
import kotlin.math.abs
import kotlin.math.roundToInt

fun Modifier.horizontalAlphaMaskLinear(
    vararg data: LinearMaskData,
    stepFactor: Float = 0.4f,
    map: (Float) -> Float = { t -> t },
): Modifier = alphaMaskLinear(
    data = data,
    isHorizontal = true,
    stepFactor = stepFactor,
    map = map,
)

fun Modifier.verticalAlphaMaskLinear(
    vararg data: LinearMaskData,
    stepFactor: Float = 0.4f,
    map: (Float) -> Float = { t -> t },
): Modifier = alphaMaskLinear(
    data = data,
    isHorizontal = false,
    stepFactor = stepFactor,
    map = map,
)

fun Modifier.alphaMaskLinear(
    vararg data: LinearMaskData,
    isHorizontal: Boolean = true,
    stepFactor: Float = 0.4f,
    map: (Float) -> Float = { t -> t },
): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithCache {
        val width = size.width
        val height = size.height
        val maxSize = if (isHorizontal) width else height

        val brushList = mutableListOf<Brush>()

        data.forEach {
            val (startDp, endDp, reverse) = it

            var startPx = if (startDp.isFinite) startDp.toPx().coerceIn(0f, maxSize) else 0f
            var endPx = if (endDp.isFinite) endDp.toPx().coerceIn(0f, maxSize) else maxSize

            if (reverse) {
                startPx = maxSize - startPx
                endPx = maxSize - endPx
            }

            val (start, end) = if (isHorizontal) {
                Offset(startPx, height / 2) to Offset(endPx, height / 2)
            } else {
                Offset(width / 2, startPx) to Offset(width / 2, endPx)
            }

            val lengthPx = abs(endPx - startPx)
            val n = (stepFactor * lengthPx).roundToInt().coerceAtLeast(2)

            val pairs = (0 until n).map { i ->
                val t = i / (n - 1f)
                val alpha = map(t).coerceIn(0f, 1f)
                t to Color.White.copy(alpha = alpha)
            }.toTypedArray()

            brushList += Brush.linearGradient(
                colorStops = pairs,
                start = start,
                end = end,
                tileMode = TileMode.Clamp
            )
        }

        onDrawWithContent {
            drawContent()
            brushList.forEach {
                drawRect(brush = it, blendMode = BlendMode.DstOut)
            }
        }
    }

data class LinearMaskData(
    val startDp: Dp = 0.dp,
    val endDp: Dp = Dp.Infinity,
    val reverse: Boolean = false,
)
