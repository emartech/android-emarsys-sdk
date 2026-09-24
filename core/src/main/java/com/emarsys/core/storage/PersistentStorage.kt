package com.emarsys.core.storage

interface PersistentStorage<T, S> : Storage<T> {
    fun persistValue(store: S, value: T)
    fun readPersistedValue(store: S): T?
    fun removePersistedValue(store: S)
}
