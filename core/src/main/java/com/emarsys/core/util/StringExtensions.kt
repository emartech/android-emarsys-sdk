package com.emarsys.core.util

import java.util.*

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
val snakeRegex = "_[a-zA-Z]".toRegex()


fun String.camelToLowerSnakeCase(): String {
    return camelRegex.replace(this) {
        "_${it.value}"
    }.lowercase(Locale.getDefault())
}

fun String.camelToUpperSnakeCase(): String {
    return camelRegex.replace(this) {
        "_${it.value}"
    }.uppercase(Locale.getDefault())
}

fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) {
        it.value.replace("_", "")
                .uppercase(Locale.getDefault())
    }
}