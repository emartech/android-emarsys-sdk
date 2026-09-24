package com.emarsys.core.storage;

public interface KeyValueStore {

    void putString(String key, String value);

    void putInt(String key, int value);

    void putLong(String key, long value);

    void putFloat(String key, float value);

    void putDouble(String key, double value);

    void putBoolean(String key, boolean value);

    String getString(String key);

    int getInt(String key);

    long getLong(String key);

    float getFloat(String key);

    double getDouble(String key);

    boolean getBoolean(String key);

    void remove(String key);

    void clear();

    int getSize();

    boolean isEmpty();
}
