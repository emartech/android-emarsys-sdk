package com.emarsys.core.device;

import android.os.Build;

import java.util.Locale;

public class LanguageProvider {

    public String provideLanguage(Locale locale) {
        String language;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            language = locale.toLanguageTag();
        } else {
            language = provideLanguageForLegacyDevices(locale);
        }
        return language;
    }

    private String provideLanguageForLegacyDevices(Locale locale) {
        return locale.getLanguage();
    }
}
