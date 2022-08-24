package xyz.xfqlittlefan.notdeveloper.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import xyz.xfqlittlefan.notdeveloper.R
import xyz.xfqlittlefan.notdeveloper.preferences.ADB_ENABLED
import xyz.xfqlittlefan.notdeveloper.preferences.ADB_WIFI_ENABLED
import xyz.xfqlittlefan.notdeveloper.preferences.DEVELOPMENT_SETTINGS_ENABLED
import xyz.xfqlittlefan.notdeveloper.ui.composables.AppBar
import xyz.xfqlittlefan.notdeveloper.ui.composables.rememberBooleanSharedPreference
import xyz.xfqlittlefan.notdeveloper.ui.theme.IAmNotADeveloperTheme
import xyz.xfqlittlefan.notdeveloper.util.allBars
import xyz.xfqlittlefan.notdeveloper.xposed.isModuleActive

class MainActivity : ComponentActivity() {
    @SuppressLint("WorldReadableFiles")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            IAmNotADeveloperTheme {
                Scaffold(
                    topBar = {
                        AppBar(
                            title = {
                                Text(stringResource(R.string.app_name))
                            }, modifier = Modifier.windowInsetsPadding(
                                WindowInsets.allBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                            )
                        )
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .windowInsetsPadding(WindowInsets.allBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var testResult by remember { mutableStateOf<List<Boolean>?>(null) }
                        var devSettings by rememberBooleanSharedPreference(
                            key = DEVELOPMENT_SETTINGS_ENABLED,
                            defaultValue = true
                        )
                        var usbDebugging by rememberBooleanSharedPreference(
                            key = ADB_ENABLED,
                            defaultValue = true
                        )
                        var wirelessDebugging by rememberBooleanSharedPreference(
                            key = ADB_WIFI_ENABLED,
                            defaultValue = true
                        )

                        ListItem(headlineText = {
                            Text(stringResource(R.string.hide_development_mode))
                        }, modifier = Modifier.clickable {
                            devSettings = !devSettings
                        }, trailingContent = {
                            Switch(
                                checked = devSettings,
                                onCheckedChange = { devSettings = it }
                            )
                        })
                        ListItem(headlineText = {
                            Text(stringResource(R.string.hide_usb_debugging))
                        }, modifier = Modifier.clickable {
                            usbDebugging = !usbDebugging
                        }, trailingContent = {
                            Switch(
                                checked = usbDebugging,
                                onCheckedChange = { usbDebugging = it }
                            )
                        })
                        ListItem(headlineText = {
                            Text(stringResource(R.string.hide_wireless_debugging))
                        }, modifier = Modifier.clickable {
                            wirelessDebugging = !wirelessDebugging
                        }, trailingContent = {
                            Switch(
                                checked = wirelessDebugging,
                                onCheckedChange = { wirelessDebugging = it }
                            )
                        })
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
                                stringResource(R.string.module_not_actived),
                                modifier = Modifier.padding(horizontal = 20.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (testResult?.size == 3) {
                            fun getString(on: String, off: String, input: List<Boolean>) =
                                input.map { if (it) on else off }.toTypedArray()

                            AlertDialog(onDismissRequest = { testResult = null }, confirmButton = {
                                Button(onClick = { testResult = null }) {
                                    Text(stringResource(android.R.string.ok))
                                }
                            }, title = {
                                Text(stringResource(R.string.test))
                            }, text = {
                                Column {
                                    Text(
                                        stringResource(
                                            R.string.dialog_test_content, *getString(
                                                stringResource(R.string.on),
                                                stringResource(R.string.off),
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