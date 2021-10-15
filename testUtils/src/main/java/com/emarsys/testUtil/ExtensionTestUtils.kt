package com.emarsys.testUtil

object ExtensionTestUtils {
    inline fun <reified T> Any.tryCast(block: T.() -> Unit) {
        if (this is T) {
            block()
        } else {
            throw IllegalArgumentException("Casted value is not the type of ${T::class.java.name}")
        }
    }
}