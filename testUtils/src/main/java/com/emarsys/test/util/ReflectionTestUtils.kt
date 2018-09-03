package com.emarsys.test.util

object ReflectionTestUtils {

    @JvmStatic
    fun setStaticField(type: Class<*>, fieldName: String, value: Any?) {
        val containerField = type.getDeclaredField(fieldName)
        containerField.isAccessible = true
        containerField.set(null, value)
    }

    @JvmStatic
    fun <T> getStaticField(type: Class<*>, fieldName: String): T? {
        val field = type.getDeclaredField(fieldName)
        field.isAccessible = true
        val result = field.get(null)
        return result as T?
    }

    @JvmStatic
    fun <T> instantiate(type: Class<*>, constructorIndex: Int, vararg args: Any): T {
        val constructor = type.declaredConstructors[constructorIndex]
        constructor.isAccessible = true
        return constructor.newInstance(*args) as T
    }

}
