package top.ltfan.notdeveloper.settings

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import top.ltfan.material.m3.settingspage.SettingsResource
import top.ltfan.material.m3.settingspage.SettingsResources
import top.ltfan.material.m3.settingspage.annotation.SettingsItem
import top.ltfan.material.m3.settingspage.annotation.SettingsStore
import top.ltfan.material.m3.settingspage.annotation.SettingsValue
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.datastore.UiSettings

/**
 * The user interface settings model. The settings page describer is generated
 * from this data class.
 */
@SettingsStore
data class UiSettingsModel(
    @SettingsItem(BlurResources::class)
    val blur: Boolean = true,

    @SettingsItem(SmoothCornersResources::class)
    val smoothRoundedCorners: Boolean = true,

    @SettingsItem(ProgressiveResources::class)
    val progressive: ProgressiveMode = ProgressiveMode.Disabled,
)

/** The progressive blur mode applied to the top bars. */
enum class ProgressiveMode : SettingsResources {
    @SettingsValue
    Disabled {
        override val label get() = SettingsResource.Composable {
            stringResource(R.string.label_settings_ui_blur_enabled_progressive_disabled)
        }
        override val description: SettingsResource<String?>? = null
        override val icon: SettingsResource<Painter?>? = null
    },

    @SettingsValue
    Mask {
        override val label get() = SettingsResource.Composable {
            stringResource(R.string.label_settings_ui_blur_enabled_progressive_mask)
        }
        override val description: SettingsResource<String?>? = null
        override val icon: SettingsResource<Painter?>? = null
    },

    @SettingsValue
    ScaledAuto {
        override val label get() = SettingsResource.Composable {
            stringResource(R.string.label_settings_ui_blur_enabled_progressive_scaled_auto)
        }
        override val description: SettingsResource<String?>? = null
        override val icon: SettingsResource<Painter?>? = null
    },

    @SettingsValue
    ScaledCustom {
        override val label get() = SettingsResource.Composable {
            stringResource(R.string.label_settings_ui_blur_enabled_progressive_scaled_custom)
        }
        override val description: SettingsResource<String?>? = null
        override val icon: SettingsResource<Painter?>? = null
    },

    @SettingsValue
    Full {
        override val label get() = SettingsResource.Composable {
            stringResource(R.string.label_settings_ui_blur_enabled_progressive_full)
        }
        override val description: SettingsResource<String?>? = null
        override val icon: SettingsResource<Painter?>? = null
    },
}

object BlurResources : SettingsResources {
    override val label get() = SettingsResource.Composable {
        stringResource(R.string.label_settings_ui_blur)
    }
    override val description: SettingsResource<String?>? = null
    override val icon: SettingsResource<Painter?>? = null
}

object SmoothCornersResources : SettingsResources {
    override val label get() = SettingsResource.Composable {
        stringResource(R.string.label_settings_ui_smooth_corner)
    }
    override val description: SettingsResource<String?>? get() = SettingsResource.Composable<String?> {
        stringResource(R.string.description_settings_ui_smooth_corner)
    }
    override val icon: SettingsResource<Painter?>? = null
}

object ProgressiveResources : SettingsResources {
    override val label get() = SettingsResource.Composable {
        stringResource(R.string.label_settings_ui_blur_enabled_progressive)
    }
    override val description: SettingsResource<String?>? = null
    override val icon: SettingsResource<Painter?>? = null
}

/** Reads the persisted settings as the settings model. */
fun UiSettings.toModel(): UiSettingsModel = UiSettingsModel(
    blur = blurSettings.value is UiSettings.BlurSettings.Value.Enabled,
    smoothRoundedCorners = smoothRoundedCorners.value,
    progressive = when (val blur = blurSettings.value) {
        is UiSettings.BlurSettings.Value.Disabled -> ProgressiveMode.Disabled
        is UiSettings.BlurSettings.Value.Enabled -> when (blur.progressiveSettings.value) {
            is UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Disabled ->
                ProgressiveMode.Disabled

            is UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Mask ->
                ProgressiveMode.Mask

            is UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Scaled.Auto ->
                ProgressiveMode.ScaledAuto

            is UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Scaled.Custom ->
                ProgressiveMode.ScaledCustom

            is UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Full ->
                ProgressiveMode.Full
        }
    },
)

/** Writes the settings model back into the persisted settings. */
fun UiSettingsModel.applyTo(current: UiSettings): UiSettings = current.copy(
    blurSettings = UiSettings.BlurSettings(
        if (blur) {
            UiSettings.BlurSettings.Value.Enabled(
                UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings(
                    when (progressive) {
                        ProgressiveMode.Disabled ->
                            UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Disabled

                        ProgressiveMode.Mask ->
                            UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Mask

                        ProgressiveMode.ScaledAuto ->
                            UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Scaled.Auto

                        ProgressiveMode.ScaledCustom ->
                            UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Scaled.Custom()

                        ProgressiveMode.Full ->
                            UiSettings.BlurSettings.Value.Enabled.ProgressiveSettings.Value.Full
                    }
                )
            )
        } else {
            UiSettings.BlurSettings.Value.Disabled
        }
    ),
    smoothRoundedCorners = UiSettings.SmoothRoundedCorners(smoothRoundedCorners),
)
