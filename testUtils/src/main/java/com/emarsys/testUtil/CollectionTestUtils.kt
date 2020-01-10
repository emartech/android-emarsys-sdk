package com.emarsys.testUtil

import java.util.*

object CollectionTestUtils {

    @JvmStatic
    fun numberOfElementsIn(list: List<Any?>, type: Class<*>): Int {
        return list
                .filterNotNull()
                .filter { it::class.java == type }
                .count()
    }

    @JvmStatic
    fun numberOfElementsIn(array: Array<Any>, type: Class<*>): Int {
        var count = 0

        for (o in array) {
            if (o.javaClass == type) {
                count++
            }
        }

        return count
    }

    @JvmStatic
    fun <T> getElementByType(list: List<*>, type: Class<T>): T? {
        for (o in list) {
            if (type.isInstance(o)) {
                return type.cast(o)
            }
        }
        throw NoSuchElementException("Cannot find element of class $type in $list")
    }
}
