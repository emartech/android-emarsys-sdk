package com.emarsys.core.storage;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.emarsys.core.util.Assert;

@SuppressLint("ApplySharedPref")
public class DefaultKeyValueStore implements KeyValueStore {

    private final SharedPreferences prefs;

    public DefaultKeyValueStore(SharedPreferences prefs) {
        Assert.notNull(prefs, "Prefs must not be null!");
        this.prefs = prefs;
    }

    @Override
    public void putString(String key, String value) {
        Assert.notNull(key, "Key must not be null!");
        Assert.notNull(value, "Value must not be null!");
        prefs.edit().putString(key, value).commit();
    }

    @Override
    public void putInt(String key, int value) {
        Assert.notNull(key, "Key must not be null!");
        prefs.edit().putInt(key, value).commit();
    }

    @Override
    public void putLong(String key, long value) {
        Assert.notNull(key, "Key must not be null!");
        prefs.edit().putLong(key, value).commit();
    }

    @Override
    public void putFloat(String key, float value) {
        Assert.notNull(key, "Key must not be null!");
        prefs.edit().putFloat(key, value).commit();
    }

    @Override
    public void putDouble(String key, double value) {
        Assert.notNull(key, "Key must not be null!");
        prefs.edit().putLong(key, Double.doubleToRawLongBits(value)).commit();
    }

    @Override
    public void putBoolean(String key, boolean value) {
        Assert.notNull(key, "Key must not be null!");
        prefs.edit().putBoolean(key, value).commit();
    }

    @Override
    public String getString(String key) {
        Assert.notNull(key, "Key must not be null!");
        return prefs.getString(key, null);
    }

    @Override
    public int getInt(String key) {
        Assert.notNull(key, "Key must not be null!");
        return prefs.getInt(key, 0);
    }

    @Override
    public long getLong(String key) {
        Assert.notNull(key, "Key must not be null!");
        return prefs.getLong(key, 0);
    }

    @Override
    public float getFloat(String key) {
        Assert.notNull(key, "Key must not be null!");
        return prefs.getFloat(key, 0);
    }

    @Override
    public double getDouble(String key) {
        Assert.notNull(key, "Key must not be null!");
        return Double.longBitsToDouble(prefs.getLong(key, 0));
    }

    @Override
    public boolean getBoolean(String key) {
        Assert.notNull(key, "Key must not be null!");
        return prefs.getBoolean(key, false);
    }

    @Override
    public void remove(String key) {
        Assert.notNull(key, "Key must not be null!");
        prefs.edit().remove(key).commit();
    }

    @Override
    public void clear() {
        prefs.edit().clear().commit();
    }

    @Override
    public int getSize() {
        return prefs.getAll().size();
    }

    @Override
    public boolean isEmpty() {
        return prefs.getAll().isEmpty();
    }

}