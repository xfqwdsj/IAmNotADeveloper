package top.ltfan.notdeveloper.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import top.ltfan.notdeveloper.ui.util.HazeZIndex
import top.ltfan.notdeveloper.ui.util.hazeSource
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun IAmNotADeveloperTheme(
    viewModel: AppViewModel,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable AppViewModel.() -> Unit,
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme) {
        CompositionLocalProvider(LocalHazeStyle provides HazeMaterials.regular()) {
            with(viewModel) {
                Box(Modifier.hazeSource(zIndex = HazeZIndex.app)) {
                    content()
                }
            }
        }
    }
}
