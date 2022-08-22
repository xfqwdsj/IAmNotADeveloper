package xyz.xfqlittlefan.notdeveloper.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import xyz.xfqlittlefan.notdeveloper.ADB_ENABLED
import xyz.xfqlittlefan.notdeveloper.ADB_WIFI_ENABLED
import xyz.xfqlittlefan.notdeveloper.DEVELOPMENT_SETTINGS_ENABLED
import xyz.xfqlittlefan.notdeveloper.R
import xyz.xfqlittlefan.notdeveloper.ui.composables.AppBar
import xyz.xfqlittlefan.notdeveloper.ui.composables.rememberBooleanSharedPreference
import xyz.xfqlittlefan.notdeveloper.ui.theme.IAmNotADeveloperTheme
import xyz.xfqlittlefan.notdeveloper.util.allBars

class MainActivity : ComponentActivity() {
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
                    var devSettings by rememberBooleanSharedPreference(
                        key = DEVELOPMENT_SETTINGS_ENABLED,
                        defaultValue = false
                    )
                    var usbDebugging by rememberBooleanSharedPreference(
                        key = ADB_ENABLED,
                        defaultValue = false
                    )
                    var wirelessDebugging by rememberBooleanSharedPreference(
                        key = ADB_WIFI_ENABLED,
                        defaultValue = false
                    )

                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .windowInsetsPadding(WindowInsets.allBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                    ) {
                        ListItem(headlineText = {
                            Text(stringResource(R.string.hide_development_mode))
                        }, trailingContent = {
                            Switch(
                                checked = devSettings,
                                onCheckedChange = { devSettings = it }
                            )
                        })
                        ListItem(headlineText = {
                            Text(stringResource(R.string.hide_usb_debugging))
                        }, trailingContent = {
                            Switch(
                                checked = usbDebugging,
                                onCheckedChange = { usbDebugging = it }
                            )
                        })
                        ListItem(headlineText = {
                            Text(stringResource(R.string.hide_wireless_debugging))
                        }, trailingContent = {
                            Switch(
                                checked = wirelessDebugging,
                                onCheckedChange = { wirelessDebugging = it }
                            )
                        })
                    }
                }
            }
        }
    }
}