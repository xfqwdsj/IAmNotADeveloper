package top.ltfan.notdeveloper.settings

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import kotlinx.serialization.Serializable
import top.ltfan.material.m3.datastore.annotation.Store
import top.ltfan.material.m3.settingspage.SettingsResource
import top.ltfan.material.m3.settingspage.SettingsResources
import top.ltfan.material.m3.settingspage.annotation.SettingsItem
import top.ltfan.material.m3.settingspage.annotation.SettingsStore
import top.ltfan.material.m3.settingspage.annotation.SettingsValue
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.datastore.AppCodec

/**
 * The user interface settings model. The settings page describer is
 * generated from this data class.
 */
@Serializable
@SettingsStore
@Store(name = "ui_settings", codec = AppCodec::class)
data class UiSettingsModel(
    @SettingsItem(BlurResources::class)
    val blur: Boolean = true,

    @SettingsItem(SmoothCornersResources::class)
    val smoothRoundedCorners: Boolean = true,

    @SettingsItem(ProgressiveResources::class, dependsOn = ["blur"])
    val progressive: ProgressiveMode = ProgressiveMode.Disabled,
)

/** The progressive blur mode applied to the top bars. */
enum class ProgressiveMode : SettingsResources {
    @SettingsValue
    Disabled {
        override val label
            get() = SettingsResource.Composable {
                stringResource(R.string.label_settings_ui_blur_enabled_progressive_disabled)
            }
        override val description: SettingsResource<String?>? = null
        override val icon: SettingsResource<Painter?>? = null
    },

    @SettingsValue
    Mask {
        override val label
            get() = SettingsResource.Composable {
                stringResource(R.string.label_settings_ui_blur_enabled_progressive_mask)
            }
        override val description: SettingsResource<String?>? = null
        override val icon: SettingsResource<Painter?>? = null
    },

    @SettingsValue
    ScaledAuto {
        override val label
            get() = SettingsResource.Composable {
                stringResource(R.string.label_settings_ui_blur_enabled_progressive_scaled_auto)
            }
        override val description: SettingsResource<String?>? = null
        override val icon: SettingsResource<Painter?>? = null
    },

    @SettingsValue
    ScaledCustom {
        override val label
            get() = SettingsResource.Composable {
                stringResource(R.string.label_settings_ui_blur_enabled_progressive_scaled_custom)
            }
        override val description: SettingsResource<String?>? = null
        override val icon: SettingsResource<Painter?>? = null
    },

    @SettingsValue
    Full {
        override val label
            get() = SettingsResource.Composable {
                stringResource(R.string.label_settings_ui_blur_enabled_progressive_full)
            }
        override val description: SettingsResource<String?>? = null
        override val icon: SettingsResource<Painter?>? = null
    },
}

object BlurResources : SettingsResources {
    override val label
        get() = SettingsResource.Composable {
            stringResource(R.string.label_settings_ui_blur)
        }
    override val description: SettingsResource<String?>? = null
    override val icon: SettingsResource<Painter?>? = null
}

object SmoothCornersResources : SettingsResources {
    override val label
        get() = SettingsResource.Composable {
            stringResource(R.string.label_settings_ui_smooth_corner)
        }
    override val description: SettingsResource<String?>
        get() = SettingsResource.Composable {
            stringResource(R.string.description_settings_ui_smooth_corner)
        }
    override val icon: SettingsResource<Painter?>? = null
}

object ProgressiveResources : SettingsResources {
    override val label
        get() = SettingsResource.Composable {
            stringResource(R.string.label_settings_ui_blur_enabled_progressive)
        }
    override val description: SettingsResource<String?>? = null
    override val icon: SettingsResource<Painter?>? = null
}


