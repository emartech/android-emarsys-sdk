package com.emarsys.mobileengage.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class AppLoginStorage implements Storage<Integer> {
    public static final String APP_LOGIN_PAYLOAD_HASH_CODE_KEY = "appLoginPayloadHashCode";

    private SharedPreferences sharedPreferences;

    public AppLoginStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAMESPACE, Context.MODE_PRIVATE);
    }

    @Override
    public Integer get() {
        if (sharedPreferences.contains(APP_LOGIN_PAYLOAD_HASH_CODE_KEY)) {
            return sharedPreferences.getInt(APP_LOGIN_PAYLOAD_HASH_CODE_KEY, 0);
        }
        return null;
    }

    @Override
    public void set(Integer hashCode) {
        sharedPreferences.edit().putInt(APP_LOGIN_PAYLOAD_HASH_CODE_KEY, hashCode).commit();
    }

    @Override
    public void remove() {
        sharedPreferences.edit().remove(APP_LOGIN_PAYLOAD_HASH_CODE_KEY).commit();
    }

}