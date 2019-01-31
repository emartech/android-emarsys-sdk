package com.emarsys.core.device

import android.os.Build
import androidx.annotation.RequiresApi
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Test
    fun testProvideLanguage_shouldReturnCorrectLanguageCode() {
        val result = languageProvider.provideLanguage(Locale.US)

        result shouldBe "en-US"
    }
}