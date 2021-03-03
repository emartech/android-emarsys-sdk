package com.emarsys.mobileengage.request.mapper

import android.os.Handler
import android.os.Looper
import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.mobileengage.testUtil.DependencyTestUtils
import com.emarsys.mobileengage.util.RequestHeaderUtils
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.URL

class DeviceEventStateRequestMapperTest {
    companion object {
        const val EVENT_SERVICE_HOST: String = Endpoint.ME_EVENT_HOST
        const val DEVICE_EVENT_STATE: String = "device-event-state"
        const val APPLICATION_CODE: String = "TEST_APP_CODE"
        const val TIMESTAMP: Long = 123L
        const val REQUEST_ID: String = "123L"
    }

    private lateinit var deviceEventStateRequestMapper: DeviceEventStateRequestMapper
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockDeviceEventStateStorage: Storage<String?>

    @Before
    fun setUp() {
        FeatureRegistry.enableFeature(InnerFeature.EVENT_SERVICE_V4)
        val mockDeviceInfo: DeviceInfo = mock {
            on { hardwareId } doReturn "TEST_HARDWARE_ID"
        }
        mockRequestContext = mock {
            on { deviceInfo } doReturn mockDeviceInfo
        }
        mockRequestModel = mock {
            on { url } doReturn URL(EVENT_SERVICE_HOST + Endpoint.inlineInAppBase(APPLICATION_CODE))
        }
        mockDeviceEventStateStorage = mock {
            on { get() } doReturn DEVICE_EVENT_STATE
        }

        DependencyTestUtils.setupDependencyInjectionWithServiceProviders()

        deviceEventStateRequestMapper = DeviceEventStateRequestMapper(mockRequestContext, mockDeviceEventStateStorage)
    }

    @After
    fun tearDown() {
        val handler = getDependency<Handler>("coreSdkHandler")
        val looper: Looper = handler.looper
        looper.quit()
        DependencyInjection.tearDown()
    }

    @Test
    fun testShouldMap_false_whenV4isDisabled() {
        FeatureRegistry.disableFeature(InnerFeature.EVENT_SERVICE_V4)

        val result = deviceEventStateRequestMapper.shouldMapRequestModel(mockRequestModel)

        result shouldBe false
    }

    @Test
    fun testShouldMap_true_whenV4isEnabled() {
        val result = deviceEventStateRequestMapper.shouldMapRequestModel(mockRequestModel)

        result shouldBe true
    }

    @Test
    fun testShouldMap_false_whenRequestModelIsNotInlineInApp() {
        whenever(mockRequestModel.url).thenReturn(URL(EVENT_SERVICE_HOST + Endpoint.eventBase(APPLICATION_CODE)))

        val result = deviceEventStateRequestMapper.shouldMapRequestModel(mockRequestModel)

        result shouldBe false
    }

    @Test
    fun testShouldMap_true_whenRequestModelIsInlineInApp() {
        val result = deviceEventStateRequestMapper.shouldMapRequestModel(mockRequestModel)

        result shouldBe true
    }

    @Test
    fun testShouldMap_false_whenStorageIsEmpty() {
        whenever(mockDeviceEventStateStorage.get()).thenReturn(null)
        val result = deviceEventStateRequestMapper.shouldMapRequestModel(mockRequestModel)

        result shouldBe false
    }

    @Test
    fun testCreatePayload_shouldAddDeviceEventState_toBody() {
        val expectedRequestPayload =
                 mapOf("deviceEventState" to DEVICE_EVENT_STATE)
        val result =
                deviceEventStateRequestMapper.createPayload(createInlineInAppRequest())

        result shouldBe expectedRequestPayload
    }

    private fun createInlineInAppRequest(
            extraHeaders: Map<String, String> = mapOf(),
            extraPayloads: Map<String, Any> = mapOf()
    ) = RequestModel(
            "https://mobile-events.eservice.emarsys.net/v4/apps/${APPLICATION_CODE}/inline-messages",
            RequestMethod.POST,
            extraPayloads,
            RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext) + extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
    )
}