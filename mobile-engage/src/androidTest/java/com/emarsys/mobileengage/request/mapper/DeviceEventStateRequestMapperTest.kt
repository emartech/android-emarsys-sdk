package com.emarsys.mobileengage.request.mapper

import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DeviceEventStateRequestMapperTest : AnnotationSpec() {
    companion object {
        const val DEVICE_EVENT_STATE: String = """{"device-event-state":true}"""
        val DEVICE_EVENT_STATE_JSON: JSONObject = JSONObject("""{"device-event-state":true}""")
        const val APPLICATION_CODE: String = "TEST_APP_CODE"
        const val TIMESTAMP: Long = 123L
        const val REQUEST_ID: String = "123L"
    }

    private lateinit var deviceEventStateRequestMapper: DeviceEventStateRequestMapper
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockDeviceEventStateStorage: Storage<String?>
    private lateinit var mockRequestModelHelper: RequestModelHelper

    @Before
    fun setUp() {
        FeatureRegistry.enableFeature(InnerFeature.EVENT_SERVICE_V4)
        val mockDeviceInfo: DeviceInfo = mock {
            on { clientId } doReturn "TEST_CLIENT_ID"
        }
        mockRequestContext = mock {
            on { deviceInfo } doReturn mockDeviceInfo
        }
        mockRequestModel = mock()
        mockDeviceEventStateStorage = mock {
            on { get() } doReturn DEVICE_EVENT_STATE
        }
        mockRequestModelHelper = mock {
            on { isCustomEvent(any()) } doReturn true
            on { isInlineInAppRequest(any()) } doReturn true
        }

        deviceEventStateRequestMapper = DeviceEventStateRequestMapper(mockRequestContext, mockRequestModelHelper, mockDeviceEventStateStorage)
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
    fun testShouldMap_true_whenRequestModelIsCustomEvent() {
        val result = deviceEventStateRequestMapper.shouldMapRequestModel(mockRequestModel)

        result shouldBe true
    }


    @Test
    fun testShouldMap_false_whenRequestModelIsNotCustemEventNorInlineInappRequest() {
        whenever(mockRequestModelHelper.isInlineInAppRequest(any())).thenReturn(false)
        whenever(mockRequestModelHelper.isCustomEvent(any())).thenReturn(false)
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
        val expectedRequestPayload = mapOf("deviceEventState" to DEVICE_EVENT_STATE_JSON)
        val result = deviceEventStateRequestMapper.createPayload(createInlineInAppRequest())

        result["deviceEventState"].toString() shouldBe expectedRequestPayload["deviceEventState"].toString()
    }

    private fun createInlineInAppRequest(extraHeaders: Map<String, String> = mapOf(), extraPayloads: Map<String, Any> = mapOf()) = RequestModel(
            "https://mobile-events.eservice.emarsys.net/v4/apps/${APPLICATION_CODE}/inline-messages",
            RequestMethod.POST,
            extraPayloads,
            extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
    )
}