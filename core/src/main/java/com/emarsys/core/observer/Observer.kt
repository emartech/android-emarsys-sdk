package com.emarsys.core.observer

interface Observer<T> {

    fun register(callback: (T) -> Unit)
    fun unregister(callback: (T) -> Unit)
    fun notify(value: T)

}