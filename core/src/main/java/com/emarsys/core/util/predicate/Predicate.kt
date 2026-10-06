package com.emarsys.core.util.predicate

fun interface Predicate<T> {
    fun evaluate(input: T): Boolean
}
