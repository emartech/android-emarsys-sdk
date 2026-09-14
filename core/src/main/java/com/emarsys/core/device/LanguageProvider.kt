package com.emarsys.core.device

import com.emarsys.core.Mockable
import java.util.Locale

@Mockable
class LanguageProvider {
    fun provideLanguage(locale: Locale): String {
        return locale.toLanguageTag()
    }
}
