package com.emarsys.mobileengage.util

import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import java.net.URL

class RequestModelUtilsTest {

    private companion object {
        const val CLIENT_HOST = "https://me-client.eservice.emarsys.net"
        const val CLIENT_BASE = "$CLIENT_HOST/v3/apps/%s/client"
        const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net"
        const val EVENT_BASE = "$EVENT_HOST/v3/apps/%s/client/events"
        const val INBOX_HOST = "https://mobile-events.eservice.emarsys.net/v3"
        const val INBOX_BASE = "$INBOX_HOST/apps/%s/inbox"
        const val REMOTE_CONFIG_HOST = "https://mobile-sdk-config.gservice.emarsys.net"
    }

    private lateinit var mockClientServiceProvider: ServiceEndpointProvider
    private lateinit var mockEventServiceProvider: ServiceEndpointProvider
    private lateinit var mockMessageInboxServiceProvider: ServiceEndpointProvider
    private lateinit var mockRequestModel: RequestModel

    @Before
    fun setUp() {
        mockClientServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(CLIENT_HOST)
        }
        mockEventServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(EVENT_HOST)
        }
        mockMessageInboxServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn(INBOX_HOST)
        }

        mockRequestModel = mock(RequestModel::class.java)
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testIsMobileEngageRequest_true_whenItIsMobileEngageClient() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(CLIENT_BASE))
        }
        val result = RequestModelUtils.isMobileEngageV3Request(mockRequestModel, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageRequest_true_whenItIsMobileEngageEvent() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(EVENT_BASE))
        }
        val result = RequestModelUtils.isMobileEngageV3Request(mockRequestModel, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageRequest_true_whenItIsMobileEngageMessageInbox() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(INBOX_BASE))
        }
        val result = RequestModelUtils.isMobileEngageV3Request(mockRequestModel, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageRequest_false_whenItIsNotMobileEngage() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL("https://not-mobile-engage.com"))
        }
        val result = RequestModelUtils.isMobileEngageV3Request(mockRequestModel, mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)

        result shouldBe false
    }

    @Test
    fun testIsCustomEvent_V3_true_whenItIsCustomEventV3Event() {
        val requestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(EVENT_BASE))
        }
        val result = RequestModelUtils.isCustomEvent_V3(requestModel, mockEventServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsRemoteConfig_V3_true_whenItIsRemoteConfigUrl() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(REMOTE_CONFIG_HOST))
        }
        val result = RequestModelUtils.isMobileEngageV3Request(mockRequestModel,
                mockClientServiceProvider, mockEventServiceProvider, mockMessageInboxServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsCustomEvent_V3_false_whenItIsNotCustomEventV3Event() {
        val requestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(CLIENT_BASE))
        }
        val result = RequestModelUtils.isCustomEvent_V3(requestModel, mockEventServiceProvider)

        result shouldBe false
    }

    @Test
    fun testIsRefreshContactTokenRequest_true_whenItIsRefreshContactTokenRequest() {
        val requestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL("$CLIENT_BASE/contact-token"))
        }
        val result = RequestModelUtils.isRefreshContactTokenRequest(requestModel, mockClientServiceProvider)

        result shouldBe true
    }

    @Test
    fun testIsRefreshContactTokenRequest_false_whenItIsNotRefreshContactTokenRequest() {
        val requestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL("$CLIENT_BASE/contact"))
        }
        val result = RequestModelUtils.isRefreshContactTokenRequest(requestModel, mockClientServiceProvider)

        result shouldBe false
    }

}