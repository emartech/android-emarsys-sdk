package com.emarsys.core

fun interface Callable<T> {
    fun call(): T
}