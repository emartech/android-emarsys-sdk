package com.emarsys.core.provider.version

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe


class VersionProviderTest : AnnotationSpec() {


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