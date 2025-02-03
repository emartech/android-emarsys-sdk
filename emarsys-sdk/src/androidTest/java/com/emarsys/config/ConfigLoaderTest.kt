package com.emarsys.config

import android.app.Application
import android.content.SharedPreferences
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class ConfigLoaderTest  {
    companion object {
        private const val SHARED_PREF_NAME = "testPrefName"
        const val APP_CODE = "testAppCode"
        const val MERCHANT_ID = "testMerchantId"
        val SHARED_PACKAGE_NAMES = listOf("shared1", "shared2")
        const val SECRET = "testSecret"
    }

    private lateinit var configLoader: ConfigLoader
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockApplication: Application

    @Before
    fun setUp() {
        mockSharedPreferences = mockk(relaxed = true)
        mockApplication = mockk(relaxed = true)
        every {
            mockApplication.getSharedPreferences(
                SHARED_PREF_NAME,
                0
            )
        } returns mockSharedPreferences

        configLoader = ConfigLoader()
    }

    @Test
    fun testLoadConfig_createsConfigFromSharedPref() {
        every {
            mockSharedPreferences.getString(
                ConfigStorageKeys.MOBILE_ENGAGE_APPLICATION_CODE.name,
                any()
            )
        } returns APP_CODE

        every {
            mockSharedPreferences.getString(
                ConfigStorageKeys.PREDICT_MERCHANT_ID.name,
                any()
            )
        } returns MERCHANT_ID

        every {
            mockSharedPreferences.getString(
                ConfigStorageKeys.ANDROID_SHARED_SECRET.name,
                any()
            )
        } returns SECRET

        every {
            mockSharedPreferences.getStringSet(
                ConfigStorageKeys.ANDROID_SHARED_PACKAGE_NAMES.name,
                any()
            )
        } returns mutableSetOf(*SHARED_PACKAGE_NAMES.toTypedArray())

        every {
            mockSharedPreferences.getBoolean(
                ConfigStorageKeys.ANDROID_VERBOSE_CONSOLE_LOGGING_ENABLED.name,
                any()
            )
        } returns false

        every {
            mockSharedPreferences.getBoolean(
                ConfigStorageKeys.ANDROID_DISABLE_AUTOMATIC_PUSH_TOKEN_SENDING.name,
                any()
            )
        } returns false

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