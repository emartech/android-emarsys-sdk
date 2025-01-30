package com.emarsys

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class EmarsysRequestModelFactoryTest  {
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
        mockTimeStampProvider = mockk(relaxed = true)
        every { mockTimeStampProvider.provideTimestamp() } returns TIMESTAMP

        mockUUIDProvider = mockk(relaxed = true)
        every { mockUUIDProvider.provideId() } returns REQUEST_ID

        mockDeviceInfo = mockk(relaxed = true)
        every { mockDeviceInfo.clientId } returns CLIENT_ID

        mockMobileEngageRequestContext = mockk(relaxed = true)
        every { mockMobileEngageRequestContext.timestampProvider } returns mockTimeStampProvider
        every { mockMobileEngageRequestContext.uuidProvider } returns mockUUIDProvider
        every { mockMobileEngageRequestContext.deviceInfo } returns mockDeviceInfo
        every { mockMobileEngageRequestContext.applicationCode } returns APPLICATION_CODE


        requestFactory = EmarsysRequestModelFactory(mockMobileEngageRequestContext)
    }

    @Test
    fun testCreateRemoteConfigRequest() {
        val expected = RequestModel.Builder(
            mockMobileEngageRequestContext.timestampProvider,
            mockMobileEngageRequestContext.uuidProvider
        )
            .method(RequestMethod.GET)
            .url("https://mobile-sdk-config.gservice.emarsys.net/$APPLICATION_CODE")
            .build()

        val result = requestFactory.createRemoteConfigRequest()

        result shouldBe expected
    }

    @Test
    fun testCreateRemoteConfigSignatureRequest() {
        val expected = RequestModel.Builder(
            mockMobileEngageRequestContext.timestampProvider,
            mockMobileEngageRequestContext.uuidProvider
        )
            .method(RequestMethod.GET)
            .url("https://mobile-sdk-config.gservice.emarsys.net/signature/$APPLICATION_CODE")
            .build()

        val result = requestFactory.createRemoteConfigSignatureRequest()

        result shouldBe expected
    }
}