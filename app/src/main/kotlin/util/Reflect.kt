package top.ltfan.notdeveloper.util

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Reflection helpers used by the system-server side of the module, replacing
 * the legacy `XposedHelpers` API.
 */
object Reflect {
    fun findClass(name: String, classLoader: ClassLoader): Class<*> =
        Class.forName(name, false, classLoader)

    fun currentApplication(): android.app.Application =
        Class.forName("android.app.ActivityThread")
            .getMethod("currentApplication")
            .invoke(null) as android.app.Application

    fun callMethod(obj: Any, name: String, vararg args: Any?): Any? =
        lookup(obj.javaClass, name, args).invoke(obj, *args)

    fun callStaticMethod(clazz: Class<*>, name: String, vararg args: Any?): Any? =
        lookupStatic(clazz, name, args).invoke(null, *args)

    fun newInstance(clazz: Class<*>, vararg args: Any?): Any =
        constructor(clazz, args).newInstance(*args)

    fun getObjectField(obj: Any, name: String): Any? = field(obj.javaClass, name).get(obj)

    fun setObjectField(obj: Any, name: String, value: Any?) =
        field(obj.javaClass, name).set(obj, value)

    private fun candidates(clazz: Class<*>, name: String): Sequence<Method> = sequence {
        var current: Class<*>? = clazz
        while (current != null && current != Any::class.java) {
            current.declaredMethods.filter { it.name == name }.forEach { yield(it) }
            current = current.superclass
        }
        yieldAll(clazz.methods.filter { it.name == name })
    }

    private fun lookup(clazz: Class<*>, name: String, args: Array<out Any?>): Method =
        candidates(clazz, name)
            .filter { it.parameterCount == args.size }
            .firstOrNull { method -> method.parameterTypes.matches(args) }
            ?.apply { isAccessible = true }
            ?: error("Method $name(${args.size} args) not found on ${clazz.name}")

    private fun lookupStatic(clazz: Class<*>, name: String, args: Array<out Any?>): Method =
        candidates(clazz, name)
            .filter { Modifier.isStatic(it.modifiers) && it.parameterCount == args.size }
            .firstOrNull { method -> method.parameterTypes.matches(args) }
            ?.apply { isAccessible = true }
            ?: error("Static method $name(${args.size} args) not found on ${clazz.name}")

    private fun constructor(clazz: Class<*>, args: Array<out Any?>) =
        clazz.constructors
            .filter { it.parameterCount == args.size }
            .firstOrNull { ctor -> ctor.parameterTypes.matches(args) }
            ?.apply { isAccessible = true }
            ?: error("Constructor(${args.size} args) not found on ${clazz.name}")

    private fun field(clazz: Class<*>, name: String): Field {
        var current: Class<*>? = clazz
        while (current != null) {
            current.declaredFields.firstOrNull { it.name == name }
                ?.let { return it.apply { isAccessible = true } }
            current = current.superclass
        }
        error("Field $name not found on ${clazz.name}")
    }

    private fun Array<Class<*>>.matches(args: Array<out Any?>): Boolean =
        indices.all { index ->
            val type = this[index]
            val arg = args[index]
            arg == null || type.isInstance(arg) || type.boxed() == arg.javaClass
        }

    private fun Class<*>.boxed(): Class<*> = when (this) {
        java.lang.Boolean.TYPE -> java.lang.Boolean::class.java
        java.lang.Byte.TYPE -> java.lang.Byte::class.java
        java.lang.Character.TYPE -> java.lang.Character::class.java
        java.lang.Short.TYPE -> java.lang.Short::class.java
        java.lang.Integer.TYPE -> java.lang.Integer::class.java
        java.lang.Long.TYPE -> java.lang.Long::class.java
        java.lang.Float.TYPE -> java.lang.Float::class.java
        java.lang.Double.TYPE -> java.lang.Double::class.java
        else -> this
    }
}
