package top.ltfan.notdeveloper.datastore

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.data.BooleanValue
import top.ltfan.notdeveloper.data.NumberType
import top.ltfan.notdeveloper.data.NumberValue
import top.ltfan.notdeveloper.data.SealedValue
import top.ltfan.notdeveloper.datastore.model.DataStoreCompanion

@Serializable
data class UiSettings(
    val blurSettings: BlurSettings = BlurSettings(),
    val smoothRoundedCorners: SmoothRoundedCorners = SmoothRoundedCorners(),
) : AppSettingsCategory {
    @Transient
    override val labelId get() = Companion.labelId

    companion object : DataStoreCompanion<UiSettings>, AppSettingsCategory {
        override val fileName = "ui_settings"
        override val default = UiSettings()
        override val labelId = R.string.label_settings_category_ui
    }

    @Serializable
    data class BlurSettings(override val value: Value = Value.Enabled()) :
        SealedValue<BlurSettings.Value> {
        @Transient
        override val labelId get() = Companion.labelId

        @Transient
        override val descriptionId get() = Companion.descriptionId

        companion object : AppSettingsItem {
            override val labelId = R.string.label_settings_ui_blur
            override val descriptionId: Int? = null
        }

        @Serializable
        sealed class Value : AppSettingsItem {
            @Serializable
            data object Disabled : Value() {
                @Transient
                override val labelId = R.string.label_settings_ui_blur_disabled

                @Transient
                override val descriptionId = null
            }

            @Serializable
            data class Enabled(
                val progressiveSettings: ProgressiveSettings = ProgressiveSettings(),
            ) : Value() {
                @Transient
                override val labelId get() = Companion.labelId

                @Transient
                override val descriptionId get() = Companion.descriptionId

                companion object : AppSettingsItem {
                    override val labelId = R.string.label_settings_ui_blur_enabled
                    override val descriptionId: Int? = null
                }

                @Serializable
                data class ProgressiveSettings(override val value: Value = Value.Full) :
                    SealedValue<ProgressiveSettings.Value> {
                    @Transient
                    override val labelId get() = Companion.labelId

                    @Transient
                    override val descriptionId get() = Companion.descriptionId

                    companion object : AppSettingsItem {
                        override val labelId = R.string.label_settings_ui_blur_enabled_progressive
                        override val descriptionId: Int? = null
                    }


                    @Serializable
                    sealed class Value : AppSettingsItem {
                        @Serializable
                        data object Disabled : Value() {
                            @Transient
                            override val labelId =
                                R.string.label_settings_ui_blur_enabled_progressive_disabled

                            @Transient
                            override val descriptionId: Int? = null
                        }

                        @Serializable
                        data object Mask : Value() {
                            @Transient
                            override val labelId =
                                R.string.label_settings_ui_blur_enabled_progressive_mask

                            @Transient
                            override val descriptionId =
                                R.string.description_settings_ui_blur_enabled_progressive_mask
                        }

                        @Serializable
                        sealed class Scaled : Value() {
                            @Transient
                            override val labelId get() = Companion.labelId

                            @Transient
                            override val descriptionId get() = Companion.descriptionId

                            companion object : AppSettingsItem {
                                override val labelId =
                                    R.string.label_settings_ui_blur_enabled_progressive_scaled
                                override val descriptionId: Int? =
                                    R.string.description_settings_ui_blur_enabled_progressive_scaled
                            }

                            @Serializable
                            data object Auto : Scaled() {
                                @Transient
                                override val labelId =
                                    R.string.label_settings_ui_blur_enabled_progressive_scaled_auto

                                @Transient
                                override val descriptionId: Int? = null
                            }

                            @Serializable
                            data class Custom(val factor: Factor = Factor()) : Scaled() {
                                constructor(factor: Float) : this(Factor(factor))

                                @Transient
                                override val labelId get() = Companion.labelId

                                @Transient
                                override val descriptionId get() = Companion.descriptionId

                                companion object : AppSettingsItem {
                                    override val labelId =
                                        R.string.label_settings_ui_blur_enabled_progressive_scaled_custom
                                    override val descriptionId: Int? = null
                                }

                                @Serializable
                                data class Factor(override val value: Float = 0.5f) :
                                    NumberValue<Float> {
                                    @Transient
                                    override val type: NumberType<Float> = NumberType.Slider(
                                        min = 0.1f,
                                        max = 1.0f,
                                        step = 0.1f,
                                    )

                                    @Transient
                                    override val labelId get() = Companion.labelId

                                    @Transient
                                    override val descriptionId get() = Companion.descriptionId

                                    companion object : AppSettingsItem {
                                        override val labelId =
                                            R.string.label_settings_ui_blur_enabled_progressive_scaled_custom_factor
                                        override val descriptionId: Int? = null
                                    }
                                }
                            }
                        }

                        @Serializable
                        data object Full : Value() {
                            @Transient
                            override val labelId =
                                R.string.label_settings_ui_blur_enabled_progressive_full

                            @Transient
                            override val descriptionId: Int? = null
                        }
                    }
                }
            }
        }
    }

    @Serializable
    data class SmoothRoundedCorners(
        override val value: Boolean = true,
    ) : BooleanValue {
        @Transient
        override val labelId get() = Companion.labelId

        @Transient
        override val descriptionId get() = Companion.descriptionId

        companion object : AppSettingsItem {
            override val labelId = R.string.label_settings_ui_smooth_corner
            override val descriptionId = R.string.description_settings_ui_smooth_corner
        }
    }
}

interface AppSettingsCategory {
    @get:StringRes
    val labelId: Int
}

interface AppSettingsItem {
    @get:StringRes
    val labelId: Int

    @get:StringRes
    val descriptionId: Int?
}
