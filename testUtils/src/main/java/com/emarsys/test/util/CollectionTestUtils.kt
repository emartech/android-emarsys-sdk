package com.emarsys.test.util

import java.util.*

object CollectionTestUtils {

    @JvmStatic
    fun numberOfElementsIn(list: List<*>, type: Class<*>): Int {
        var count = 0

        for (o in list) {
            if (o != null && o::class.java == type) {
                count++
            }
        }

        return count
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
    fun <T> getElementByType(list: List<*>, type: Class<T>): T {
        for (o in list) {
            if (type.isInstance(o)) {
                return type.cast(o)
            }
        }
        throw NoSuchElementException("Cannot find element of class $type in $list")
    }
}
