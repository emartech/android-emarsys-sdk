package com.emarsys.core.device;

import com.emarsys.core.Mockable;
import com.emarsys.core.util.Assert;

import java.util.Locale;

@Mockable
public class LanguageProvider {

    public String provideLanguage(Locale locale) {
        Assert.notNull(locale, "Locale must not be null!");
        return locale.toLanguageTag();
    }
}
