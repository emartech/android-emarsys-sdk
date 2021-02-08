package com.emarsys.mobileengage.util

import android.os.Handler
import android.os.Looper
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.util.RequestModelUtils.isCustomEvent
import com.emarsys.mobileengage.util.RequestModelUtils.isMobileEngageRequest
import com.emarsys.mobileengage.util.RequestModelUtils.isRefreshContactTokenRequest
import com.emarsys.mobileengage.util.RequestModelUtils.isRemoteConfigRequest
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import java.net.URL

class RequestModelUtilsTest {

    private companion object {
        const val CLIENT_HOST = "https://me-client.eservice.emarsys.net/v3"
        const val CLIENT_BASE = "$CLIENT_HOST/apps/%s/client"
        const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net"
        const val EVENT_BASE = "$EVENT_HOST/v3/apps/%s/client/events"
        const val EVENT_BASE_V4 = "$EVENT_HOST/v4/apps/%s/client/events"
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
        DependencyInjection.setup(
                FakeMobileEngageDependencyContainer(
                        clientServiceProvider = mockClientServiceProvider,
                        eventServiceProvider = mockEventServiceProvider,
                        messageInboxServiceProvider = mockMessageInboxServiceProvider
                ))
    }

    @After
    fun tearDown() {
        val handler = getDependency<Handler>("coreSdkHandler")
        val looper: Looper? = handler.looper
        looper?.quit()
        DependencyInjection.tearDown()
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testIsMobileEngageRequest_true_whenItIsMobileEngageClient() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(CLIENT_BASE))
        }
        val result = mockRequestModel.isMobileEngageRequest()

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageRequest_true_whenItIsMobileEngageEvent() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(EVENT_BASE))
        }
        val result = mockRequestModel.isMobileEngageRequest()

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageRequest_true_whenItIsMobileEngageEventV4() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(EVENT_BASE_V4))
        }
        val result = mockRequestModel.isMobileEngageRequest()

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageRequest_true_whenItIsMobileEngageMessageInbox() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(INBOX_BASE))
        }
        val result = mockRequestModel.isMobileEngageRequest()

        result shouldBe true
    }

    @Test
    fun testIsMobileEngageRequest_false_whenItIsNotMobileEngage() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL("https://not-mobile-engage.com"))
        }
        val result = mockRequestModel.isMobileEngageRequest()

        result shouldBe false
    }

    @Test
    fun testIsCustomEvent_V3_true_whenItIsCustomEventV3Event() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(EVENT_BASE))
        }
        val result = mockRequestModel.isCustomEvent()

        result shouldBe true
    }

    @Test
    fun testIsRemoteConfig_true_whenItIsRemoteConfigUrl() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(REMOTE_CONFIG_HOST))
        }
        val result = mockRequestModel.isRemoteConfigRequest()

        result shouldBe true
    }

    @Test
    fun testIsCustomEvent_V3_false_whenItIsNotCustomEventV3Event() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(CLIENT_BASE))
        }
        val result = mockRequestModel.isCustomEvent()

        result shouldBe false
    }

    @Test
    fun testIsRefreshContactTokenRequest_true_whenItIsRefreshContactTokenRequest() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL("$CLIENT_BASE/contact-token"))
        }
        val result = mockRequestModel.isRefreshContactTokenRequest()

        result shouldBe true
    }

    @Test
    fun testIsRefreshContactTokenRequest_false_whenItIsNotRefreshContactTokenRequest() {
        val mockRequestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL("$CLIENT_BASE/contact"))
        }
        val result = mockRequestModel.isRefreshContactTokenRequest()

        result shouldBe false
    }

}