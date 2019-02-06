package com.emarsys.core.device;

import android.os.Build;

import com.emarsys.core.util.Assert;

import java.util.Locale;

/**
 * the implementation of provideLanguageForLegacyDevices is from apache/cordova-plugin-globalization
 * original source: https://github.com/apache/cordova-plugin-globalization/blob/master/src/android/Globalization.java
 */
public class LanguageProvider {
    private static final String SEPARATOR = "-";

    public String provideLanguage(Locale locale) {
        Assert.notNull(locale, "Locale must not be null!");

        String language;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            language = locale.toLanguageTag();
        } else {
            language = provideLanguageForLegacyDevices(locale);
        }

        return language;
    }

    private String provideLanguageForLegacyDevices(Locale locale) {
        String language = locale.getLanguage();
        String region = locale.getCountry();
        String variant = locale.getVariant();

        if (language.equals("no") && region.equals("NO") && variant.equals("NY")) {
            language = "nn";
            variant = "";
        }

        if (language.isEmpty() || !language.matches("\\p{Alpha}{2,8}")) {
            language = "und";
        } else if (language.equals("iw")) {
            language = "he";
        } else if (language.equals("in")) {
            language = "id";
        } else if (language.equals("ji")) {
            language = "yi";
        }

        if (!region.matches("\\p{Alpha}{2}|\\p{Digit}{3}")) {
            region = "";
        }

        if (!variant.matches("\\p{Alnum}{5,8}|\\p{Digit}\\p{Alnum}{3}")) {
            variant = "";
        }

        StringBuilder bcp47Tag = new StringBuilder(language);
        if (!region.isEmpty()) {
            bcp47Tag.append(SEPARATOR).append(region);
        }
        if (!variant.isEmpty()) {
            bcp47Tag.append(SEPARATOR).append(variant);
        }

        return bcp47Tag.toString();
    }

}
