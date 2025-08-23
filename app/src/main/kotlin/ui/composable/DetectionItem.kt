package top.ltfan.notdeveloper.ui.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.data.PackageInfoWrapper
import top.ltfan.notdeveloper.detection.DetectionMethod
import top.ltfan.notdeveloper.ui.theme.ListItemColorsTransparent
import top.ltfan.notdeveloper.ui.util.collectAsMutableState
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@Composable
context(viewModel: AppViewModel)
fun DetectionItem(
    method: DetectionMethod,
    packageInfo: PackageInfoWrapper? = null,
    afterChange: (DetectionMethod) -> Unit = {},
    afterTest: (DetectionMethod, Boolean) -> Unit = { _, _ -> },
    testTrigger: SharedFlow<DetectionMethod>,
    enabled: Boolean,
) {
    val coroutineScope = rememberCoroutineScope()

    val dao = remember { viewModel.application.database.dao() }
    val flow = remember(method, packageInfo) {
        if (packageInfo != null) {
            dao.isDetectionEnabledFlow(packageInfo.toDatabaseInfo(), method)
        } else {
            dao.isGlobalDetectionEnabledFlow(method)
        }
    }

    var value by flow.collectAsMutableState(true) {
        if (packageInfo != null) {
            coroutineScope.launch(Dispatchers.IO) {
                dao.insertDetection(
                    packageInfo = packageInfo.toDatabaseInfo(),
                    method = method,
                    enabled = it,
                )
                afterChange(method)
            }
        } else {
            coroutineScope.launch(Dispatchers.IO) {
                dao.insertGlobalDetection(
                    method = method,
                    enabled = it,
                )
                afterChange(method)
            }
        }
    }

    var testResult by remember(method, packageInfo) {
        mutableStateOf(
            if (packageInfo != null) {
                method.test(viewModel.application)
            } else true
        )
    }

    val description = if (testResult) {
        stringResource(R.string.test_result_on)
    } else {
        stringResource(R.string.test_result_off)
    }

    PreferenceItem(
        value = value,
        onValueChange = { value = it },
        headlineContent = {
            Text(stringResource(method.labelResId))
        },
        modifier = Modifier.semantics {
            contentDescription = description
        },
        enabled = enabled,
        leadingContent = {
            if (packageInfo == null) return@PreferenceItem
            if (testResult) {
                Icon(
                    Icons.Outlined.Error,
                    contentDescription = null,
                )
            } else {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                )
            }
        },
        colors = ListItemColorsTransparent,
    )

    LaunchedEffect(Unit) {
        testTrigger.collect {
            testResult = method.test(viewModel.application).also {
                afterTest(method, it)
            }
        }
    }
}
