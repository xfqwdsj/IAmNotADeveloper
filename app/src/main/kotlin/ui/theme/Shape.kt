package top.ltfan.notdeveloper.ui.theme

import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import top.ltfan.material.m3.core.graphic.ContinuousRoundedCornerShape

val AppRadiusExtraSmall = 8.dp
val AppExtraSmallShape = ContinuousRoundedCornerShape(AppRadiusExtraSmall)

val AppRadiusSmall = 12.dp
val AppSmallShape = ContinuousRoundedCornerShape(AppRadiusSmall)

val AppRadiusMedium = 16.dp
val AppMediumShape = ContinuousRoundedCornerShape(AppRadiusMedium)

val AppRadiusLarge = 24.dp
val AppLargeShape = ContinuousRoundedCornerShape(AppRadiusLarge)

val AppRadiusExtraLarge = 36.dp
val AppExtraLargeShape = ContinuousRoundedCornerShape(AppRadiusExtraLarge)

val AppShapes = Shapes(
    extraSmall = AppExtraSmallShape,
    small = AppSmallShape,
    medium = AppMediumShape,
    large = AppLargeShape,
    extraLarge = AppExtraLargeShape,
)
