package com.emarsys.core.util

object SystemUtils {

    fun isClassFound(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (ignored: ClassNotFoundException) {
            false
        }
    }

    fun getCallerMethodName(): String {
        return Thread.currentThread().stackTrace[3].methodName
    }
}
