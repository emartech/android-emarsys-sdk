package com.emarsys.testUtil

import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

object ReflectionTestUtils {

    @JvmStatic
    fun setInstanceField(instance: Any, fieldName: String, value: Any?) =
        setField(instance, instance.javaClass, fieldName, value)

    @JvmStatic
    fun setStaticField(type: Class<*>, fieldName: String, value: Any?) =
        setField(null, type, fieldName, value)

    private fun setField(instance: Any?, type: Class<*>, fieldName: String, value: Any?) {
        val containerField = searchForField(type, fieldName)
        containerField.isAccessible = true
        containerField.set(instance, value)
    }

    @JvmStatic
    fun <T> getInstanceField(instance: Any, fieldName: String): T? =
        getField(instance, instance::class.java, fieldName)

    @JvmStatic
    fun <T> getStaticField(type: Class<*>, fieldName: String): T? =
        getField(null, type, fieldName)

    @Suppress("UNCHECKED_CAST")
    private fun <T> getField(instance: Any?, type: Class<*>, fieldName: String): T? {
        val field = searchForField(type, fieldName)
        field.isAccessible = true
        val result = field.get(instance)
        return result as T?
    }

    fun setCompanionField(instance: Any, fieldName: String, value: Any?) {
        val property = instance::class.memberProperties.find { it.name == fieldName }
        if (property is KMutableProperty<*>) {
            property.setter.call(instance, value)
        }
    }

    fun <T> getCompanionField(instance: Any, fieldName: String): Any? {
        val property = instance::class.memberProperties.find { it.name == fieldName }
        return property?.getter?.call(instance)
    }


    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> instantiate(type: Class<*>, constructorIndex: Int, vararg args: Any): T {
        val constructor = type.declaredConstructors[constructorIndex]
        constructor.isAccessible = true
        return constructor.newInstance(*args) as T
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun invokeStaticMethod(type: Class<*>, methodName: String, vararg parameters: Any) {
        val parameterTypes = parameters.map { it::class.javaPrimitiveType }.toTypedArray()
        val method = type.getDeclaredMethod(methodName, *parameterTypes)
        method.isAccessible = true
        method.invoke(null, *parameters)
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> invokeInstanceMethod(
        instance: Any,
        methodName: String,
        vararg params: Pair<Class<*>, *>
    ): T {
        val types = params.map { it.first }.toTypedArray()
        val method: Method = instance.javaClass.getDeclaredMethod(methodName, *types)
        method.isAccessible = true

        return (method.invoke(instance, *params.map { it.second }.toTypedArray()) as T)
    }

    fun setKotlinDelegateField(instance: Any, fieldName: String, value: Any?) {
        val property = instance::class.memberProperties.find { it.name == fieldName }
        if (property is KMutableProperty<*>) {
            property.setter.call(value, "value")
        }
    }

    private fun searchForField(type: Class<*>, fieldName: String): Field = try {
        type.getDeclaredField(fieldName)
    } catch (nsfe: NoSuchFieldException) {
        nsfe
    }.let { result ->
        when (result) {
            is NoSuchFieldException -> when (val superclass = type.superclass) {
                null -> throw NoSuchFieldException("Could not find field in class hierarchy!")
                else -> searchForField(superclass, fieldName)
            }
            is Field -> result
            else -> throw IllegalStateException("Unrecognized type: ${result.javaClass}")
        }
    }

}
