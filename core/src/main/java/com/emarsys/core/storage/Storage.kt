package com.emarsys.core.storage

interface Storage<T> {
    fun set(value: T)
    fun get(): T
    fun remove()
}
