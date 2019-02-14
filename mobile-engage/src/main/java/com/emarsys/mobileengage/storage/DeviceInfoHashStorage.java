package com.emarsys.mobileengage.storage;

import android.content.SharedPreferences;

import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.Assert;

public class DeviceInfoHashStorage implements Storage<Integer> {
    public static final String DEVICE_INFO_HASH_CODE_KEY = "deviceInfoHashCode";

    private final SharedPreferences sharedPreferences;

    public DeviceInfoHashStorage(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void set(Integer hash) {
        Assert.notNull(hash, "Hash must not be null!");

        sharedPreferences.edit().putInt(DEVICE_INFO_HASH_CODE_KEY, hash).commit();
    }

    @Override
    public Integer get() {
        if (sharedPreferences.contains(DEVICE_INFO_HASH_CODE_KEY)) {
            return sharedPreferences.getInt(DEVICE_INFO_HASH_CODE_KEY, 0);
        }
        return null;
    }

    @Override
    public void remove() {
        sharedPreferences.edit().remove(DEVICE_INFO_HASH_CODE_KEY).commit();
    }
}
