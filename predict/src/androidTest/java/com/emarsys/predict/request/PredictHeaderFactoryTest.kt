package com.emarsys.predict.request

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.testUtil.mockito.whenever
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

import org.mockito.Mockito

class PredictHeaderFactoryTest {
    private companion object {
        const val OS_VERSION = "1.0.0"
        const val PLATFORM = "android"
    }

    private lateinit var headerFactory: PredictHeaderFactory
    private lateinit var mockRequestContext: PredictRequestContext
    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockKeyValueStore: KeyValueStore


    @BeforeEach
    fun setUp() {
        mockKeyValueStore = Mockito.mock(KeyValueStore::class.java)

        mockDeviceInfo = Mockito.mock(DeviceInfo::class.java).apply {
            whenever(platform).thenReturn(PLATFORM)
            whenever(osVersion).thenReturn(OS_VERSION)
        }

        mockRequestContext = Mockito.mock(PredictRequestContext::class.java).apply {
            whenever(keyValueStore).thenReturn(mockKeyValueStore)
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
        }
        headerFactory = PredictHeaderFactory(mockRequestContext)
    }

    @Test
    fun testConstructor_requestContext_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            PredictHeaderFactory(null)
        }
    }

    @Test
    fun testCreateBaseHeader() {
        val expected = mapOf("User-Agent" to "EmarsysSDK|osversion:$OS_VERSION|platform:$PLATFORM")

        val result = headerFactory.createBaseHeader()

        result shouldBe expected
    }

    @Test
    fun testCreateBaseHeader_shouldPutXpInCookiesInHeader_whenXpIsNotNull() {
        whenever(mockKeyValueStore.getString("xp")).thenReturn("testXpCookie")
        val expected = mapOf(
            "User-Agent" to "EmarsysSDK|osversion:$OS_VERSION|platform:$PLATFORM",
            "Cookie" to "xp=testXpCookie;"
        )

        val result = headerFactory.createBaseHeader()

        result shouldBe expected
    }

    @Test
    fun testCreateBaseHeader_shouldPutVisitorIdInCookiesInHeader_whenVisitorIdIsNotNull() {
        whenever(mockKeyValueStore.getString("predict_visitor_id")).thenReturn("testVisitorId")
        val expected = mapOf(
            "User-Agent" to "EmarsysSDK|osversion:$OS_VERSION|platform:$PLATFORM",
            "Cookie" to "cdv=testVisitorId"
        )

        val result = headerFactory.createBaseHeader()

        result shouldBe expected
    }

    @Test
    fun testCreateBaseHeader_shouldPutVisitorIdAndXpInCookiesInHeader_whenBothAvailable() {
        whenever(mockKeyValueStore.getString("xp")).thenReturn("testXpCookie")
        whenever(mockKeyValueStore.getString("predict_visitor_id")).thenReturn("testVisitorId")
        val expected = mapOf(
            "User-Agent" to "EmarsysSDK|osversion:$OS_VERSION|platform:$PLATFORM",
            "Cookie" to "xp=testXpCookie;cdv=testVisitorId"
        )

        val result = headerFactory.createBaseHeader()

        result shouldBe expected
    }

    @Test
    fun testCreateBaseHeader_shouldNotIncludeCookie_whenNeitherIsAvailable() {
        val expected = mapOf("User-Agent" to "EmarsysSDK|osversion:$OS_VERSION|platform:$PLATFORM")

        val result = headerFactory.createBaseHeader()

        result shouldBe expected
    }
}