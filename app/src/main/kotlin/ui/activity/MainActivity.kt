package top.ltfan.notdeveloper.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation3.ui.NavDisplay
import top.ltfan.notdeveloper.ui.theme.IAmNotADeveloperTheme
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel
import top.ltfan.notdeveloper.util.isMiui
import top.ltfan.notdeveloper.xposed.notDevService
import top.ltfan.notdeveloper.xposed.statusIsPreferencesReady

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        @Suppress("DEPRECATION") if (isMiui) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
        }
        super.onCreate(savedInstanceState)

        setContent {
            IAmNotADeveloperTheme {
                NavDisplay(
                    backStack = viewModel.backStack,
                    entryProvider = { it.navEntry(viewModel) },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.isPreferencesReady = statusIsPreferencesReady
        if (viewModel.service == null) {
            viewModel.service = notDevService
        }
        viewModel.test()
    }
}
