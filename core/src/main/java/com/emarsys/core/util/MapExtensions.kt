package com.emarsys.core.util

fun <T> Map<String, T>.getCaseInsensitive(key: String): T? {
    var result: T? = null
    for (originalKey in this.keys) {
        if (originalKey.toLowerCase() == key.toLowerCase()) {
            result = this[originalKey]
            break
        }
    }

    return result
}