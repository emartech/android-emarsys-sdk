package com.emarsys.mobileengage.storage;

import android.content.SharedPreferences;

import com.emarsys.core.util.Assert;

public class RefreshTokenStorage implements Storage<String> {
    public static final String REFRESH_TOKEN_KEY = "refreshToken";

    private final SharedPreferences sharedPreferences;

    public RefreshTokenStorage(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void set(String refreshToken) {
        Assert.notNull(refreshToken, "RefreshToken must not be null!");

        sharedPreferences.edit().putString(REFRESH_TOKEN_KEY, refreshToken).commit();
    }

    @Override
    public String get() {
        if (sharedPreferences.contains(REFRESH_TOKEN_KEY)) {
            return sharedPreferences.getString(REFRESH_TOKEN_KEY, null);
        }
        return null;
    }

    @Override
    public void remove() {
        sharedPreferences.edit().remove(REFRESH_TOKEN_KEY).commit();
    }
}
