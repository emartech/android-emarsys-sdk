package com.emarsys.core

interface Mapper<T, V> {
    fun map(value: T): V
}