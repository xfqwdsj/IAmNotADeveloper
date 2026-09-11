package top.ltfan.notdeveloper.data

import kotlinx.serialization.Serializable
import top.ltfan.notdeveloper.datastore.AppSettingsItem

sealed interface AppSettingsValue<T> : AppSettingsItem {
    val value: T
}

interface SealedValue<T> : AppSettingsValue<T>

interface NumberValue<T : Number> : AppSettingsValue<T> {
    val type: NumberType<T>
}

@Serializable
sealed class NumberType<T : Number> {
    @Serializable
    class Input<T : Number> : NumberType<T>()

    @Serializable
    data class Slider<T : Number>(
        val min: T,
        val max: T,
        val step: T,
    ) : NumberType<T>()

    @Serializable
    data class Select<T : Number>(
        val min: T,
        val max: T,
        val step: T,
    ) : NumberType<T>()
}

interface BooleanValue : AppSettingsValue<Boolean>
