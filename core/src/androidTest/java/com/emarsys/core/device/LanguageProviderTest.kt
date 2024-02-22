package com.emarsys.core.device

import android.os.Build.VERSION_CODES.P
import androidx.test.filters.SdkSuppress
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

class LanguageProviderTest {


    private lateinit var languageProvider: LanguageProvider

    @BeforeEach
    fun setUp() {
        languageProvider = LanguageProvider()
    }

    @Test
    fun testProvideLanguage_localeMustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            languageProvider.provideLanguage(null)
        }
    }

    @Test
    fun testProvideLanguage_shouldReturnCorrectLanguageCode_whenOnlyLanguageUsed() {
        val result = languageProvider.provideLanguage(Locale("en"))

        result shouldBe "en"
    }

    @Test
    fun testProvideLanguage_shouldReturnCorrectLanguageCode_whenLanguageAndCountryUsed() {
        val result = languageProvider.provideLanguage(Locale("en", "US"))

        result shouldBe "en-US"
    }

    @Test
    @SdkSuppress(minSdkVersion = P)
    fun testProvideLanguage_shouldReturnCorrectLanguageCode_whenLocaleBuilderUsed() {
        val result = languageProvider.provideLanguage(
            Locale.Builder().setLanguage("zh").setScript("Hans").setRegion("CN").build()
        )

        result shouldBe "zh-Hans-CN"
    }

    @Test
    fun testProvideLanguage_shouldReturnCorrectLanguageCode() {
        val result = languageProvider.provideLanguage(Locale.SIMPLIFIED_CHINESE)

        result shouldBe "zh-CN"
    }
}