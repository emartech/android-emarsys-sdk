package com.emarsys.mobileengage.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class MeIdStorage implements Storage<String> {
    public static final String ME_ID_KEY = "meId";

    private SharedPreferences sharedPreferences;

    public MeIdStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAMESPACE, Context.MODE_PRIVATE);
    }

    @Override
    public String get() {
        return sharedPreferences.getString(ME_ID_KEY, null);
    }

    @Override
    public void set(String meId) {
        sharedPreferences.edit().putString(ME_ID_KEY, meId).commit();
    }

    @Override
    public void remove() {
        sharedPreferences.edit().remove(ME_ID_KEY).commit();
    }

}