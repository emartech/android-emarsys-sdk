package com.emarsys.core.device

import android.os.Build.VERSION_CODES.P
import androidx.test.filters.SdkSuppress
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.*

class LanguageProviderTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var languageProvider: LanguageProvider

    @Before
    fun setUp() {
        languageProvider = LanguageProvider()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testProvideLanguage_localeMustNotBeNull() {
        languageProvider.provideLanguage(null)
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
        val result = languageProvider.provideLanguage(Locale.Builder().setLanguage("zh").setScript("Hans").setRegion("CN").build())

        result shouldBe "zh-Hans-CN"
    }

    @Test
    fun testProvideLanguage_shouldReturnCorrectLanguageCode() {
        val result = languageProvider.provideLanguage(Locale.SIMPLIFIED_CHINESE)

        result shouldBe "zh-CN"
    }
}