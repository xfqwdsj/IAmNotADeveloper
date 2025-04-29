package top.ltfan.notdeveloper.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.ltfan.notdeveloper.ADB_ENABLED
import top.ltfan.notdeveloper.ADB_WIFI_ENABLED
import top.ltfan.notdeveloper.DEVELOPMENT_SETTINGS_ENABLED
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.ui.composables.rememberBooleanSharedPreference
import top.ltfan.notdeveloper.ui.theme.IAmNotADeveloperTheme
import top.ltfan.notdeveloper.xposed.isModuleActive
import top.ltfan.notdeveloper.xposed.isPreferencesReady
import kotlin.reflect.full.declaredFunctions

class MainActivity : ComponentActivity() {
    private val isMiui: Boolean
        @SuppressLint("PrivateApi") get() {
            val clazz = Class.forName("android.os.SystemProperties").kotlin
            val method =
                clazz.declaredFunctions.firstOrNull { it.name == "get" && it.parameters.size == 1 }
            return method?.call("ro.miui.ui.version.name") != ""
        }

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
                        var testResult by remember { mutableStateOf<List<Boolean>?>(null) }
                        if (isPreferencesReady()) {
                            @Suppress("DEPRECATION") var devSettings by rememberBooleanSharedPreference(
                                mode = Context.MODE_WORLD_READABLE,
                                key = DEVELOPMENT_SETTINGS_ENABLED,
                                defaultValue = true
                            )
                            @Suppress("DEPRECATION") var usbDebugging by rememberBooleanSharedPreference(
                                mode = Context.MODE_WORLD_READABLE,
                                key = ADB_ENABLED,
                                defaultValue = true
                            )
                            @Suppress("DEPRECATION") var wirelessDebugging by rememberBooleanSharedPreference(
                                mode = Context.MODE_WORLD_READABLE,
                                key = ADB_WIFI_ENABLED,
                                defaultValue = true
                            )

                            ListItem(headlineContent = {
                                Text(stringResource(R.string.hide_development_mode))
                            }, modifier = Modifier.clickable {
                                devSettings = !devSettings
                            }, trailingContent = {
                                Switch(
                                    checked = devSettings,
                                    onCheckedChange = { devSettings = it }
                                )
                            })
                            ListItem(headlineContent = {
                                Text(stringResource(R.string.hide_usb_debugging))
                            }, modifier = Modifier.clickable {
                                usbDebugging = !usbDebugging
                            }, trailingContent = {
                                Switch(
                                    checked = usbDebugging,
                                    onCheckedChange = { usbDebugging = it }
                                )
                            })
                            ListItem(headlineContent = {
                                Text(stringResource(R.string.hide_wireless_debugging))
                            }, modifier = Modifier.clickable {
                                wirelessDebugging = !wirelessDebugging
                            }, trailingContent = {
                                Switch(
                                    checked = wirelessDebugging,
                                    onCheckedChange = { wirelessDebugging = it }
                                )
                            })
                        } else {
                            Spacer(Modifier.height(20.dp))
                            Text(
                                stringResource(R.string.unable_to_save_settings),
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = {
                            val result = mutableListOf<Boolean>()
                            result.add(
                                Settings.Global.getInt(
                                    contentResolver,
                                    DEVELOPMENT_SETTINGS_ENABLED,
                                    0
                                ) == 1
                            )
                            result.add(Settings.Global.getInt(contentResolver, ADB_ENABLED, 0) == 1)
                            result.add(
                                Settings.Global.getInt(
                                    contentResolver,
                                    ADB_WIFI_ENABLED,
                                    0
                                ) == 1
                            )
                            testResult = result
                        }) {
                            Text(stringResource(R.string.test))
                        }
                        Spacer(Modifier.height(20.dp))
                        if (isModuleActive) {
                            Text(
                                stringResource(R.string.description),
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        } else {
                            Text(
                                stringResource(R.string.module_not_activated),
                                modifier = Modifier.padding(horizontal = 20.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(Modifier.height(20.dp))

                        if (testResult?.size == 3) {
                            fun getString(on: String, off: String, input: List<Boolean>) =
                                input.map { if (it) on else off }.toTypedArray()

                            AlertDialog(onDismissRequest = { testResult = null }, confirmButton = {
                                TextButton(onClick = { testResult = null }) {
                                    Text(stringResource(android.R.string.ok))
                                }
                            }, title = {
                                Text(stringResource(R.string.test))
                            }, text = {
                                Column {
                                    Text(
                                        stringResource(
                                            R.string.dialog_test_content, *getString(
                                                stringResource(R.string.status_on),
                                                stringResource(R.string.status_off),
                                                testResult ?: listOf(false, false, false)
                                            )
                                        )
                                    )
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}
