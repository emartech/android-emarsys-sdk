package com.emarsys.core.storage

import android.content.SharedPreferences
import androidx.core.content.edit

class BooleanStorage(key: StorageKey, store: SharedPreferences) : AbstractStorage<Boolean, SharedPreferences>(store) {
    val key: String = key.key

    override fun persistValue(store: SharedPreferences, value: Boolean) {
        store.edit { putBoolean(key, value) }
    }

    override fun readPersistedValue(store: SharedPreferences): Boolean {
        return store.getBoolean(key, false)
    }

    override fun removePersistedValue(store: SharedPreferences) {
        store.edit { remove(key) }
    }
}