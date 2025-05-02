package top.ltfan.notdeveloper.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.ltfan.notdeveloper.Item
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.ui.composable.PreferenceItem
import top.ltfan.notdeveloper.ui.composable.StatusCard
import top.ltfan.notdeveloper.ui.composable.rememberBooleanSharedPreference
import top.ltfan.notdeveloper.ui.theme.IAmNotADeveloperTheme
import top.ltfan.notdeveloper.util.isMiui
import top.ltfan.notdeveloper.xposed.statusIsPreferencesReady

class MainActivity : ComponentActivity() {
    private var isPreferencesReady by mutableStateOf(false)
    private val testResults = mutableStateMapOf<Item, Boolean>()

    @SuppressLint("WorldReadableFiles")
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
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .consumeWindowInsets(padding)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(padding)
                            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(16.dp))

                        StatusCard(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            isPreferencesReady = isPreferencesReady
                        )

                        Spacer(Modifier.height(16.dp))

                        for (item in Item.entries) {
                            @Suppress("DEPRECATION") var pref by rememberBooleanSharedPreference(
                                mode = MODE_WORLD_READABLE,
                                key = item.key,
                                defaultValue = true,
                                afterSet = { check() }
                            )
                            val testResult = testResults[item] ?: false

                            PreferenceItem(
                                nameId = item.nameId,
                                testResult = testResult,
                                checked = pref,
                                onClick = { pref = !pref },
                                enabled = isPreferencesReady
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isPreferencesReady = statusIsPreferencesReady
        check()
    }

    private fun check() {
        Item.settingGlobalItems.forEach {
            testResults[it] = Settings.Global.getInt(
                contentResolver,
                it.key,
                0
            ) == 1
        }
    }
}
