package com.emarsys

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext

import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class EmarsysRequestModelFactoryTest : AnnotationSpec() {

    companion object {
        const val CLIENT_ID = "client_id"
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val APPLICATION_CODE = "applicationCode"
    }

    private lateinit var mockUUIDProvider: UUIDProvider
    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockTimeStampProvider: TimestampProvider
    private lateinit var requestFactory: EmarsysRequestModelFactory
    private lateinit var mockMobileEngageRequestContext: MobileEngageRequestContext


    @Before
    fun setUp() {
        mockTimeStampProvider = mock {
            on { provideTimestamp() } doReturn TIMESTAMP
        }

        mockUUIDProvider = mock {
            on { provideId() } doReturn REQUEST_ID
        }

        mockDeviceInfo = mock {
            on { clientId } doReturn CLIENT_ID
        }

        mockMobileEngageRequestContext = mock {
            on { timestampProvider } doReturn mockTimeStampProvider
            on { uuidProvider } doReturn mockUUIDProvider
            on { deviceInfo } doReturn mockDeviceInfo
            on { applicationCode } doReturn APPLICATION_CODE
        }

        requestFactory = EmarsysRequestModelFactory(mockMobileEngageRequestContext)
    }

    @Test
    fun testCreateRemoteConfigRequest() {
        val expected = RequestModel.Builder(mockMobileEngageRequestContext.timestampProvider, mockMobileEngageRequestContext.uuidProvider)
                .method(RequestMethod.GET)
                .url("https://mobile-sdk-config.gservice.emarsys.net/$APPLICATION_CODE")
                .build()

        val result = requestFactory.createRemoteConfigRequest()

        result shouldBe expected
    }

    @Test
    fun testCreateRemoteConfigSignatureRequest() {
        val expected = RequestModel.Builder(mockMobileEngageRequestContext.timestampProvider, mockMobileEngageRequestContext.uuidProvider)
                .method(RequestMethod.GET)
                .url("https://mobile-sdk-config.gservice.emarsys.net/signature/$APPLICATION_CODE")
                .build()

        val result = requestFactory.createRemoteConfigSignatureRequest()

        result shouldBe expected
    }
}