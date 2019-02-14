package com.emarsys.mobileengage.storage;

import android.content.SharedPreferences;

import com.emarsys.core.storage.Storage;

public class MeIdSignatureStorage implements Storage<String> {
    public static final String ME_ID_SIGNATURE_KEY = "meIdSignature";

    private SharedPreferences sharedPreferences;

    public MeIdSignatureStorage(SharedPreferences prefs) {
        sharedPreferences = prefs;
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