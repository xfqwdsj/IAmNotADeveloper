package top.ltfan.notdeveloper.ui.theme

import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle

val AppRadiusExtraSmall = 8.dp
val AppExtraSmallShape = ContinuousRoundedRectangle(AppRadiusExtraSmall)

val AppRadiusSmall = 12.dp
val AppSmallShape = ContinuousRoundedRectangle(AppRadiusSmall)

val AppRadiusMedium = 16.dp
val AppMediumShape = ContinuousRoundedRectangle(AppRadiusMedium)

val AppRadiusLarge = 24.dp
val AppLargeShape = ContinuousRoundedRectangle(AppRadiusLarge)

val AppRadiusExtraLarge = 36.dp
val AppExtraLargeShape = ContinuousRoundedRectangle(AppRadiusExtraLarge)

val AppShapes = Shapes(
    extraSmall = AppExtraSmallShape,
    small = AppSmallShape,
    medium = AppMediumShape,
    large = AppLargeShape,
    extraLarge = AppExtraLargeShape,
)
