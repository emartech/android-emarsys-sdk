package com.emarsys.core.device

import android.os.Build.VERSION_CODES.LOLLIPOP
import androidx.test.filters.SdkSuppress
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test
import java.util.*

class LanguageProviderTest {

    private lateinit var languageProvider: LanguageProvider

    @Before
    fun setUp() {
        languageProvider = LanguageProvider()
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    fun testProvideLanguage_shouldReturnCorrectLanguageCode() {
        val result = languageProvider.provideLanguage(Locale.US)

        result shouldBe "en-US"
    }
}