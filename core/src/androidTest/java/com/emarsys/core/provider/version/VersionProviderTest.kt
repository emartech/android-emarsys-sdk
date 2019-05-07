package com.emarsys.core.provider.version

import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class VersionProviderTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

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