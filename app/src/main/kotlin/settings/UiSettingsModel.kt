package top.ltfan.notdeveloper.settings

import androidx.compose.ui.res.stringResource
import kotlinx.serialization.Serializable
import top.ltfan.m3x.datastore.annotation.Store
import top.ltfan.m3x.resource.Resource
import top.ltfan.m3x.resource.compose.composeResource
import top.ltfan.m3x.settingspage.SettingsResources
import top.ltfan.m3x.settingspage.annotation.SettingsItem
import top.ltfan.m3x.settingspage.annotation.SettingsStore
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.datastore.AppCodec

/**
 * The user interface settings model. The settings page describer is generated from this data class.
 */
@Serializable
@SettingsStore
@Store(name = "ui_settings", codec = AppCodec::class)
data class UiSettingsModel(
    @SettingsItem(BlurResources::class) val blur: Boolean = true,
    @SettingsItem(SmoothCornersResources::class) val smoothRoundedCorners: Boolean = true,
    @SettingsItem(ProgressiveResources::class, dependsOn = ["blur"])
    val progressive: Boolean = true,
)

object BlurResources : SettingsResources {
    override val label
        get() = composeResource {
            stringResource(R.string.label_settings_ui_blur)
        }
}

object SmoothCornersResources : SettingsResources {
    override val label
        get() = composeResource {
            stringResource(R.string.label_settings_ui_smooth_corner)
        }

    override val description: Resource<String?>
        get() = composeResource {
            stringResource(R.string.description_settings_ui_smooth_corner)
        }
}

object ProgressiveResources : SettingsResources {
    override val label
        get() = composeResource {
            stringResource(R.string.label_settings_ui_blur_enabled_progressive)
        }
}
