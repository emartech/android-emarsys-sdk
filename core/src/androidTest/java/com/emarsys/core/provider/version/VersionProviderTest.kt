package com.emarsys.core.provider.version

import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test

class VersionProviderTest  {


    private lateinit var versionProvider: VersionProvider

    @Before
    fun setUp() {
        versionProvider = VersionProvider()
    }

    @Test
    fun testProvideSdkVersion() {
        val expected = com.emarsys.core.BuildConfig.VERSION_NAME

        val sdkVersion = versionProvider.provideSdkVersion()

        sdkVersion shouldBe expected
    }
}