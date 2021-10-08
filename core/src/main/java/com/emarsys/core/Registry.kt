package com.emarsys.core

interface Registry<K, V> {
    fun register(key: K, value: V)
}