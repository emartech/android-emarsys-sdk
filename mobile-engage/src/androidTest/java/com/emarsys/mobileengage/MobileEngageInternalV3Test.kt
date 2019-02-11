package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*


class MobileEngageInternalV3Test {

    companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hardware_id"
        const val APPLICATION_CODE = "app_code"
        const val SET_PUSH_TOKEN_URL = "https://ems-me-client.herokuapp.com/v3/apps/$APPLICATION_CODE/client/push-token"
        const val PUSH_TOKEN = "kjhygtfdrtrtdtguyihoj3iurf8y7t6fqyua2gyi8fhu"
    }

    lateinit var mobileEngageInternal: MobileEngageInternalV3

    lateinit var requestManagerMock: RequestManager
    lateinit var requestContextMock: RequestContext
    lateinit var completionListener: CompletionListener

    lateinit var timestampProviderMock: TimestampProvider
    lateinit var uuidProviderMock: UUIDProvider
    lateinit var deviceInfoMock: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        uuidProviderMock = mock(UUIDProvider::class.java).apply {
            whenever(provideId()).thenReturn(REQUEST_ID)
        }
        timestampProviderMock = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }
        deviceInfoMock = mock(DeviceInfo::class.java).apply {
            whenever(hwid).thenReturn(HARDWARE_ID)
        }

        requestManagerMock = mock(RequestManager::class.java)
        requestContextMock = mock(RequestContext::class.java).apply {
            whenever(timestampProvider).thenReturn(timestampProviderMock)
            whenever(uuidProvider).thenReturn(uuidProviderMock)
            whenever(deviceInfo).thenReturn(deviceInfoMock)
            whenever(applicationCode).thenReturn(APPLICATION_CODE)
        }

        completionListener = mock(CompletionListener::class.java)

        mobileEngageInternal = MobileEngageInternalV3(requestManagerMock, requestContextMock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestManager_mustNotBeNull() {
        MobileEngageInternalV3(null, requestContextMock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestContext_mustNotBeNull() {
        MobileEngageInternalV3(requestManagerMock, null)
    }

    @Test
    fun testSetPushToken() {
        val expectedRequestModel = createSetPushTokenRequestModel(PUSH_TOKEN)

        mobileEngageInternal.setPushToken(PUSH_TOKEN, completionListener)

        verify(requestManagerMock).submit(expectedRequestModel, completionListener)
    }

    @Test
    fun testSetPushToken_completionListener_canBeNull() {
        val expectedRequestModel = createSetPushTokenRequestModel(PUSH_TOKEN)

        mobileEngageInternal.setPushToken(PUSH_TOKEN, null)

        verify(requestManagerMock).submit(expectedRequestModel, null)
    }


    @Test
    fun testSetPushToken_whenPushTokenIsNull_callShouldBeIgnored() {
        mobileEngageInternal.setPushToken(null, completionListener)

        verifyZeroInteractions(requestManagerMock)
    }

    private fun createSetPushTokenRequestModel(pushToken: String): RequestModel {
        return RequestModel(
                SET_PUSH_TOKEN_URL,
                RequestMethod.PUT,
                mapOf(
                        "pushToken" to pushToken
                ),
                mapOf(
                        "X-CLIENT-ID" to HARDWARE_ID,
                        "X-REQUEST-ORDER" to TIMESTAMP.toString()
                ),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )
    }
}