package com.emarsys.core.util

inline fun <reified T> Any.tryCastOrNull(): T? {
    if (this is T) {
        return this
    }
    return null
}
