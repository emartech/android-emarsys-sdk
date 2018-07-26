package com.emarsys.mobileengage.testUtil;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.storage.Storage;

public class SharedPrefsUtils {
    public static final String MOBILE_ENGAGE_SHARED_PREFS = Storage.SHARED_PREFERENCES_NAMESPACE;

    private SharedPrefsUtils() {
    }

    public static void deleteMobileEngageSharedPrefs() {
        InstrumentationRegistry
                .getTargetContext()
                .getSharedPreferences(MOBILE_ENGAGE_SHARED_PREFS, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

}
