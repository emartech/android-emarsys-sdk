package com.emarsys.mobileengage.storage;

import com.emarsys.core.util.Assert;

public abstract class AbstractStorage<T, S> implements PersistentStorage<T, S> {

    private final S store;

    private T value;

    public AbstractStorage(S store) {
        Assert.notNull(store, "Store must not be null!");

        this.store = store;
    }

    @Override
    public void set(T item) {
        value = item;
        persistValue(store, item);
    }

    @Override
    public T get() {
        value = value != null ? value : readPersistedValue(store);
        return value;
    }

    @Override
    public void remove() {
        value = null;
        removePersistedValue(store);
    }

}
