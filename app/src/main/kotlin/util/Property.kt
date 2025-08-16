package top.ltfan.notdeveloper.util

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T, V> mutableProperty(value: V, setValue: (V) -> Unit) = object : ReadWriteProperty<T, V> {
    override fun getValue(thisRef: T, property: KProperty<*>): V {
        return value
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        setValue(value)
    }
}
