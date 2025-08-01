package top.ltfan.notdeveloper.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.detection.DetectionCategory
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.service.INotDevService
import top.ltfan.notdeveloper.ui.composable.CategoryCard
import top.ltfan.notdeveloper.ui.composable.StatusCard
import top.ltfan.notdeveloper.ui.theme.IAmNotADeveloperTheme
import top.ltfan.notdeveloper.util.isMiui
import top.ltfan.notdeveloper.xposed.Log
import top.ltfan.notdeveloper.xposed.notDevService
import top.ltfan.notdeveloper.xposed.notifySettingChange
import top.ltfan.notdeveloper.xposed.statusIsPreferencesReady

class MainActivity : ComponentActivity() {
    private var isPreferencesReady by mutableStateOf(false)
    private val testResults = mutableStateMapOf<DetectionMethod, Boolean>()
    private var service: INotDevService? by mutableStateOf(null)

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
                val scrollBehavior =
                    TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        LargeTopAppBar(
                            title = {
                                Text(stringResource(R.string.app_name))
                            },
                            windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                            scrollBehavior = scrollBehavior
                        )
                    },
                ) { padding ->
                    val layoutDirection = LocalLayoutDirection.current
                    val insets = WindowInsets.displayCutout
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                        .asPaddingValues()
                    val contentPadding = PaddingValues(
                        start = padding.calculateStartPadding(layoutDirection) + insets.calculateStartPadding(
                            layoutDirection
                        ),
                        top = padding.calculateTopPadding() + insets.calculateTopPadding() + 16.dp,
                        end = padding.calculateEndPadding(layoutDirection) + insets.calculateEndPadding(
                            layoutDirection
                        ),
                        bottom = padding.calculateBottomPadding() + insets.calculateBottomPadding() + 16.dp,
                    )
                    LazyColumn(
                        modifier = Modifier
                            .consumeWindowInsets(contentPadding)
                            .fillMaxSize(),
                        contentPadding = contentPadding,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            StatusCard(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                isPreferencesReady = isPreferencesReady,
                                isServiceConnected = service != null,
                            )
                        }

                        items(DetectionCategory.values) { category ->
                            CategoryCard(
                                category = category,
                                testResults = testResults,
                                afterChange = ::afterChange,
                                isPreferencesReady = isPreferencesReady,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun afterChange(method: DetectionMethod) {
        when (method) {
            is DetectionMethod.SettingsMethod -> {
                val service = service
                if (service == null) {
                    Log.Android.w("Service not connected, cannot notify settings changes")
                    return
                }

                try {
                    service.notifySettingChange(method) {
                        check()
                    }
                } catch (e: Throwable) {
                    Log.Android.e("Failed to notify setting change for ${method.preferenceKey}", e)
                    check()
                }
            }

            is DetectionMethod.SystemPropertiesMethod -> check()
        }
    }

    override fun onResume() {
        super.onResume()
        isPreferencesReady = statusIsPreferencesReady
        if (service == null) {
            service = notDevService
        }
        check()
    }

    private fun check() {
        DetectionCategory.allMethods.forEach { method ->
            val result = method.test(this)
            testResults[method] = result
            Log.v("${method.preferenceKey} test result: $result")
        }
    }
}
