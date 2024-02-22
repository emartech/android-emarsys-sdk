package com.emarsys.config

import android.app.Application
import android.content.SharedPreferences
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ConfigLoaderTest {
    companion object {
        private const val SHARED_PREF_NAME = "testPrefName"
        const val APP_CODE = "testAppCode"
        const val MERCHANT_ID = "testMerchantId"
        val SHARED_PACKAGE_NAMES = listOf("shared1", "shared2")
        const val SECRET = "testSecret"
    }

    private lateinit var configLoader: ConfigLoader
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockApplication : Application

    @BeforeEach
    fun setUp() {
        mockSharedPreferences = mock()
        mockApplication = mock {
            on { getSharedPreferences(SHARED_PREF_NAME, 0) } doReturn mockSharedPreferences
        }
        configLoader = ConfigLoader()
    }

    @Test
    fun testLoadConfig_createsConfigFromSharedPref() {
        whenever(
                mockSharedPreferences.getString(
                      eq(ConfigStorageKeys.MOBILE_ENGAGE_APPLICATION_CODE.name),
                        anyOrNull()
                )
        ) doReturn APP_CODE

        whenever(
                mockSharedPreferences.getString(
                       eq(ConfigStorageKeys.PREDICT_MERCHANT_ID.name),
                        anyOrNull()
                )
        ) doReturn MERCHANT_ID

        whenever(
                mockSharedPreferences.getString(
                       eq(ConfigStorageKeys.ANDROID_SHARED_SECRET.name),
                        anyOrNull()
                )
        ) doReturn SECRET

        whenever(
                mockSharedPreferences.getStringSet(
                        eq(ConfigStorageKeys.ANDROID_SHARED_PACKAGE_NAMES.name),
                        anyOrNull()
                )
        ) doReturn mutableSetOf(*SHARED_PACKAGE_NAMES.toTypedArray())

        whenever(
                mockSharedPreferences.getBoolean(
                        eq(ConfigStorageKeys.ANDROID_VERBOSE_CONSOLE_LOGGING_ENABLED.name),
                        anyOrNull()
                )
        ) doReturn false

        whenever(
                mockSharedPreferences.getBoolean(
                        eq(ConfigStorageKeys.ANDROID_DISABLE_AUTOMATIC_PUSH_TOKEN_SENDING.name),
                        anyOrNull()
                )
        ) doReturn false

        val expectedConfig = EmarsysConfig(
                application = mockApplication,
                applicationCode = APP_CODE,
                merchantId = MERCHANT_ID,
                sharedPackageNames = SHARED_PACKAGE_NAMES,
                sharedSecret = SECRET
        )

        val result = configLoader.loadConfigFromSharedPref(mockApplication, SHARED_PREF_NAME)

        result.build() shouldBe expectedConfig
    }

}