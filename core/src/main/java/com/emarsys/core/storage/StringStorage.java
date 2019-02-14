package com.emarsys.core.storage;

import android.content.SharedPreferences;

import com.emarsys.core.util.Assert;

public class StringStorage extends AbstractStorage<String, SharedPreferences> {

    private final String key;

    public StringStorage(StorageKey key, SharedPreferences store) {
        super(store);
        Assert.notNull(key, "Key must not be null!");
        Assert.notNull(store, "Store must not be null!");
        Assert.notNull(key.getKey(), "Key.getKey() must not be null!");

        this.key = key.getKey();
    }

    @Override
    public void persistValue(SharedPreferences store, String value) {
        store.edit().putString(key, value).apply();
    }

    @Override
    public String readPersistedValue(SharedPreferences store) {
        return store.getString(key, null);
    }

    @Override
    public void removePersistedValue(SharedPreferences store) {
        store.edit().remove(key).apply();
    }
}
