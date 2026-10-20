package top.ltfan.notdeveloper.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import top.ltfan.m3x.material.bundle.M3xMaterialTheme
import top.ltfan.m3x.material.theme.RoundedM3xShapeFactory
import top.ltfan.m3x.material.theming.capsule.CapsuleM3xShapeFactory
import top.ltfan.notdeveloper.ui.util.appBarBackdropTheme
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@Composable
fun IAmNotADeveloperTheme(
    viewModel: AppViewModel,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable AppViewModel.() -> Unit,
) {
    val colorScheme =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }

    M3xMaterialTheme(
        colorScheme = colorScheme,
        backdrop = appBarBackdropTheme(viewModel.progressive),
        shapeFactory =
            if (viewModel.smoothRoundedCorners) CapsuleM3xShapeFactory else RoundedM3xShapeFactory,
    ) {
        with(viewModel) {
            content()
        }
    }
}
