package com.emarsys.core.provider.version

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test


class VersionProviderTest {


    private lateinit var versionProvider: VersionProvider

    @BeforeEach
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