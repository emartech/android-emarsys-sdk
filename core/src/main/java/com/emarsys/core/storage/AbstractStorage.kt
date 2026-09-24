package com.emarsys.core.storage

abstract class AbstractStorage<T, S>(private val store: S) : PersistentStorage<T, S> {

    private var value: T? = null

    override fun set(item: T) {
        value = item
        persistValue(store, item)
    }

    override fun get(): T? {
        value = value ?: readPersistedValue(store)
        return value
    }

    override fun remove() {
        value = null
        removePersistedValue(store)
    }
}
