package com.emarsys.mobileengage.storage;

public interface PersistentStorage<T, S> extends Storage<T>{
    void persistValue(S store, T value);

    T readPersistedValue(S store);

    void removePersistedValue(S store);
}
