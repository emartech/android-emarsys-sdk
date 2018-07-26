package com.emarsys.mobileengage.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class MeIdSignatureStorage implements Storage<String> {
    public static final String ME_ID_SIGNATURE_KEY = "meIdSignature";

    private SharedPreferences sharedPreferences;

    public MeIdSignatureStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAMESPACE, Context.MODE_PRIVATE);
    }

    @Override
    public String get() {
        return sharedPreferences.getString(ME_ID_SIGNATURE_KEY, null);
    }

    @Override
    public void set(String meIdSignature) {
        sharedPreferences.edit().putString(ME_ID_SIGNATURE_KEY, meIdSignature).commit();
    }

    @Override
    public void remove() {
        sharedPreferences.edit().remove(ME_ID_SIGNATURE_KEY).commit();
    }

}