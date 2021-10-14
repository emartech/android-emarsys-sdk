package com.emarsys.mobileengage.request.mapper

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DefaultRequestHeaderMapperTest {

    private companion object {
        const val TIMESTAMP = 234123L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hwid"
        const val APPLICATION_CODE = "applicationCode"
        const val SDK_VERSION = "sdkVersion"
    }

    private lateinit var defaultRequestHeaderMapper: DefaultRequestHeaderMapper
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockDeviceInfo: DeviceInfo


    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockDeviceInfo = mock {
            on { hardwareId } doReturn HARDWARE_ID
            on { sdkVersion } doReturn SDK_VERSION
            on { isDebugMode } doReturn true
        }

        mockRequestContext = mock {
            on { applicationCode } doReturn APPLICATION_CODE
            on { deviceInfo } doReturn mockDeviceInfo
        }

        defaultRequestHeaderMapper = DefaultRequestHeaderMapper(mockRequestContext)
    }

    @Test
    fun testMap_shouldAddHeaders_debug() {
        val originalRequestModels = createMobileEngageRequest()

        val expectedRequestModels = createMobileEngageRequest(extraHeaders = mapOf(
            "Content-Type" to "application/json",
            "X-EMARSYS-SDK-VERSION" to mockRequestContext.deviceInfo.sdkVersion,
            "X-EMARSYS-SDK-MODE" to "debug"
        ))

        val result = defaultRequestHeaderMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
        result shouldNotBe originalRequestModels
    }

    @Test
    fun testMap_shouldAddHeaders_prod() {
        val originalRequestModels = createMobileEngageRequest()
        whenever(mockDeviceInfo.isDebugMode).thenReturn(false)

        val expectedRequestModels = createMobileEngageRequest(extraHeaders = mapOf(
            "Content-Type" to "application/json",
            "X-EMARSYS-SDK-VERSION" to mockRequestContext.deviceInfo.sdkVersion,
            "X-EMARSYS-SDK-MODE" to "production"
        ))

        val result = defaultRequestHeaderMapper.map(originalRequestModels)

        result shouldBe expectedRequestModels
        result shouldNotBe originalRequestModels
    }

    private fun createMobileEngageRequest(extraHeaders: Map<String, String> = mapOf()) = RequestModel(
        "https://me-client.eservice.emarsys.net/v3/apps/${APPLICATION_CODE}/client",
        RequestMethod.POST,
        null,
        extraHeaders,
        TIMESTAMP,
        Long.MAX_VALUE,
        REQUEST_ID
    )

}