package com.emarsys.core.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import com.emarsys.core.Mockable

@Mockable
class StringStorage(key: StorageKey, store: SharedPreferences) :
    AbstractStorage<String?, SharedPreferences>(store) {

    private val key: String = key.key

    override fun persistValue(store: SharedPreferences, value: String?) {
        store.edit { putString(key, value) }
    }

    override fun readPersistedValue(store: SharedPreferences): String? {
        return store.getString(key, null)
    }

    override fun removePersistedValue(store: SharedPreferences) {
        store.edit { remove(key) }
    }
}
