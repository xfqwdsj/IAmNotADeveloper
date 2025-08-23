package top.ltfan.notdeveloper.ui.theme

import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import com.kyant.capsule.G2RoundedCornerShape

val AppRadiusExtraSmall = 8.dp
val AppExtraSmallShape = G2RoundedCornerShape(AppRadiusExtraSmall)

val AppRadiusSmall = 12.dp
val AppSmallShape = G2RoundedCornerShape(AppRadiusSmall)

val AppRadiusMedium = 16.dp
val AppMediumShape = G2RoundedCornerShape(AppRadiusMedium)

val AppRadiusLarge = 24.dp
val AppLargeShape = G2RoundedCornerShape(AppRadiusLarge)

val AppRadiusExtraLarge = 36.dp
val AppExtraLargeShape = G2RoundedCornerShape(AppRadiusExtraLarge)

val AppShapes = Shapes(
    extraSmall = AppExtraSmallShape,
    small = AppSmallShape,
    medium = AppMediumShape,
    large = AppLargeShape,
    extraLarge = AppExtraLargeShape,
)
