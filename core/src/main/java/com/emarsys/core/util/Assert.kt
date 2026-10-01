package com.emarsys.core.util

object Assert {

    @JvmStatic
    fun notNull(`object`: Any?, message: String?) {
        if (`object` == null) {
            throw IllegalArgumentException(message ?: "Argument must not be null!")
        }
    }

    @JvmStatic
    fun positiveInt(integer: Int?, message: String?) {
        notNull(integer, null)
        if (integer!! < 1) {
            throw IllegalArgumentException(message ?: "Argument must be greater than zero!")
        }
    }

    @JvmStatic
    fun elementsNotNull(array: Array<*>?, message: String?) {
        notNull(array, null)
        for (`object` in array!!) {
            notNull(`object`, message)
        }
    }

    @JvmStatic
    fun elementsNotNull(list: List<*>?, message: String?) {
        notNull(list, null)
        for (`object` in list!!) {
            notNull(`object`, message)
        }
    }

    @JvmStatic
    fun notEmpty(array: Array<*>?, message: String?) {
        notNull(array, null)
        if (array!!.isEmpty()) {
            throw IllegalArgumentException(message ?: "Argument must not be empty!")
        }
    }

    @JvmStatic
    fun notEmpty(list: List<*>?, message: String?) {
        notNull(list, null)
        if (list!!.isEmpty()) {
            throw IllegalArgumentException(message ?: "Argument must not be empty!")
        }
    }
}
