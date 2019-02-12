package com.emarsys.mobileengage.storage;

import android.content.SharedPreferences;

import com.emarsys.core.util.Assert;

public class ContactTokenStorage implements Storage<String> {
    public static final String CONTACT_TOKEN_KEY = "contactToken";

    private final SharedPreferences sharedPreferences;

    public ContactTokenStorage(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void set(String contactToken) {
        Assert.notNull(contactToken, "ContactToken must not be null!");

        sharedPreferences.edit().putString(CONTACT_TOKEN_KEY, contactToken).commit();
    }

    @Override
    public String get() {
        if (sharedPreferences.contains(CONTACT_TOKEN_KEY)) {
            return sharedPreferences.getString(CONTACT_TOKEN_KEY, null);
        }
        return null;
    }

    @Override
    public void remove() {
        sharedPreferences.edit().remove(CONTACT_TOKEN_KEY).commit();
    }
}
