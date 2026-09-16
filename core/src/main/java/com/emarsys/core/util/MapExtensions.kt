package com.emarsys.core.util

fun <T> Map<String?, T?>.getCaseInsensitive(key: String?): T? {
    var result: T? = null
    for (originalKey in this.keys) {
        if (originalKey?.lowercase() == key?.lowercase()) {
            result = this[originalKey]
            break
        }
    }

    return result
}

fun <K, V> Map<out K?, V?>.filterNotNull(): Map<K, V> {
    val result = HashMap<K, V>()
    forEach {
        Pair(it.key, it.value).safeLet { key, value ->
            result[key] = value
        }
    }
    return result
}