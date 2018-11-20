package com.emarsys.core.provider.hardwareid;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.emarsys.core.util.Assert;

public class HardwareIdProvider {

    private static final String HARDWARE_ID_KEY = "hardwareId";

    private final Context context;
    private final SharedPreferences sharedPrefs;

    public HardwareIdProvider(Context context, SharedPreferences sharedPrefs) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(sharedPrefs, "SharedPrefs must not be null!");

        this.context = context;
        this.sharedPrefs = sharedPrefs;
    }

    public String provideHardwareId() {
        String hardwareId = sharedPrefs.getString(HARDWARE_ID_KEY, null);
        if (hardwareId == null) {
            hardwareId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            sharedPrefs.edit().putString(HARDWARE_ID_KEY, hardwareId).commit();
        }
        return hardwareId;
    }
}
