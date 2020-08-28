package com.emarsys.inapp.ui

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.fake.FakeRestClient
import com.emarsys.feature.InnerFeature
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.inline.InlineInAppWebViewFactory
import com.emarsys.mobileengage.iam.jsbridge.*
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.ReflectionTestUtils
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class InlineInAppViewTest {
    private companion object {
        const val VIEW_ID = "testViewId"
    }

    private lateinit var context: Context
    private lateinit var mockInlineInAppWebViewFactory: InlineInAppWebViewFactory
    private lateinit var mockIamJsBridgeFactory: IamJsBridgeFactory
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory
    private lateinit var webView: WebView
    private lateinit var mockProvider: CompletionHandlerProxyProvider
    private lateinit var mockJsBridge: IamJsBridge
    private lateinit var mockButtonClickedRepository: Repository<ButtonClicked, SqlSpecification>
    private lateinit var mockInAppInternal: InAppInternal

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getTargetContext()
        webView = mock {
            on { layoutParams } doReturn ViewGroup.LayoutParams(10, 10)
        }
        mockInlineInAppWebViewFactory = mock { on { create(any()) } doReturn webView }

        mockJsBridge = mock()
        mockIamJsBridgeFactory = mock { on { createJsBridge() } doReturn mockJsBridge }
        mockRequestModel = mock {
            on { id } doReturn "requestId"
        }
        mockResponseModel = mock {
            on { requestModel } doReturn mockRequestModel
        }
        mockProvider = mock {
            on { provideProxy(isNull(), any()) } doAnswer {
                it.arguments[1] as CoreCompletionHandler
            }
            on { provideProxy(any(), any()) } doAnswer {
                it.arguments[1] as CoreCompletionHandler
            }
        }
        mockRequestManager = spy(RequestManager(CoreSdkHandlerProvider().provideHandler(), mock(), mock(), mock(), FakeRestClient(mockResponseModel, FakeRestClient.Mode.SUCCESS), mock(), mock(), mockProvider))
        mockRequestModelFactory = mock {
            on { createFetchInlineInAppMessagesRequest("testViewId") }.doReturn(mockRequestModel)
        }
        mockButtonClickedRepository = mock()
        mockInAppInternal = mock()

        DependencyInjection.setup(FakeDependencyContainer(inlineInAppWebViewFactory = mockInlineInAppWebViewFactory,
                iamJsBridgeFactory = mockIamJsBridgeFactory,
                requestManager = mockRequestManager,
                requestModelFactory = mockRequestModelFactory,
                inAppInternal = mockInAppInternal,
                buttonClickedRepository = mockButtonClickedRepository))
    }

    @After
    fun tearDown() {
        DependencyInjection.tearDown()
    }

    @Test
    fun testFilterMessagesById() {
        val expectedBody = """{"inlineMessages":[
                                |{"campaignId":"765","html":"<html>Hello World</html>","viewId":"$VIEW_ID"},
                                |{"campaignId":"7625","html":"<html>Hello World2</html>","viewId":"${VIEW_ID}2"}],"oldCampaigns":[]}""".trimMargin()
        whenever(mockResponseModel.body).thenReturn(expectedBody)

        val latch = CountDownLatch(1)
        val inlineInAppView = InlineInAppView(context)
        inlineInAppView.onCompletionListener = CompletionListener { latch.countDown() }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()
        val expectedHtml = "<html>Hello World</html>"

        verify(webView).loadDataWithBaseURL(null, expectedHtml, "text/html; charset=utf-8", "UTF-8", null)
    }

    @Test
    fun testFilterMessagesById_whenViewId_isMissing() {
        val expectedBody = """{"inlineMessages":[
                                |{"campaignId":"765","html":"<html>Hello World</html>"},
                                |{"campaignId":"7625","html":"<html>Hello World2</html>"}],"oldCampaigns":[]}""".trimMargin()
        whenever(mockResponseModel.body).thenReturn(expectedBody)

        val latch = CountDownLatch(1)
        val inlineInAppView = InlineInAppView(context)
        inlineInAppView.onCompletionListener = CompletionListener { latch.countDown() }
        inlineInAppView.loadInApp(VIEW_ID)

        latch.await()

        verify(webView).loadDataWithBaseURL(null, null, "text/html; charset=utf-8", "UTF-8", null)
    }

    @Test
    fun testFilterMessagesById_whenInlineMessages_isMissing() {
        val expectedBody = """{}""".trimMargin()
        whenever(mockResponseModel.body).thenReturn(expectedBody)

        val latch = CountDownLatch(1)
        val inlineInAppView = InlineInAppView(context)
        inlineInAppView.onCompletionListener = CompletionListener { latch.countDown() }
        inlineInAppView.loadInApp(VIEW_ID)

        latch.await()

        verify(webView).loadDataWithBaseURL(null, null, "text/html; charset=utf-8", "UTF-8", null)
    }

    @Test
    fun testLoadInApp_whenResponseError() {
        DependencyInjection.tearDown()
        val expectedBody = """errorBody""".trimMargin()
        val expectedStatusCode = 500
        val expectedMessage = "Error message"
        whenever(mockResponseModel.body).thenReturn(expectedBody)
        whenever(mockResponseModel.message).thenReturn(expectedMessage)
        whenever(mockResponseModel.statusCode).thenReturn(expectedStatusCode)
        mockRequestManager = spy(RequestManager(CoreSdkHandlerProvider().provideHandler(), mock(), mock(), mock(), FakeRestClient(mockResponseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL), mock(), mock(), mockProvider))
        DependencyInjection.setup(FakeDependencyContainer(inlineInAppWebViewFactory = mockInlineInAppWebViewFactory, iamJsBridgeFactory = mockIamJsBridgeFactory, requestManager = mockRequestManager, requestModelFactory = mockRequestModelFactory))
        val inlineInAppView = InlineInAppView(context)

        val latch = CountDownLatch(1)
        inlineInAppView.onCompletionListener = CompletionListener { error ->
            error shouldNotBe null
            (error as ResponseErrorException).body shouldBe expectedBody
            error.statusCode shouldBe expectedStatusCode
            error.statusMessage shouldBe expectedMessage
            latch.countDown()
        }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        verify(webView).loadDataWithBaseURL(null, null, "text/html; charset=utf-8", "UTF-8", null)
    }

    @Test
    fun testLoadInApp_whenException() {
        DependencyInjection.tearDown()
        val expectedException = Exception("Error happened")
        mockRequestManager = spy(RequestManager(CoreSdkHandlerProvider().provideHandler(), mock(), mock(), mock(), FakeRestClient(expectedException), mock(), mock(), mockProvider))
        DependencyInjection.setup(FakeDependencyContainer(inlineInAppWebViewFactory = mockInlineInAppWebViewFactory, iamJsBridgeFactory = mockIamJsBridgeFactory, requestManager = mockRequestManager, requestModelFactory = mockRequestModelFactory))
        val inlineInAppView = InlineInAppView(context)
        val latch = CountDownLatch(1)
        inlineInAppView.onCompletionListener = CompletionListener { error ->
            error shouldBe expectedException
            latch.countDown()
        }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        verify(webView).loadDataWithBaseURL(null, null, "text/html; charset=utf-8", "UTF-8", null)
    }

    @Test
    fun testSetOnCloseEvent_shouldSetOnCloseListenerOnJSBridge() {
        val inlineInAppView = InlineInAppView(context)
        val mockOnCloseListener: OnCloseListener = mock()

        inlineInAppView.onCloseListener = mockOnCloseListener

        verify(mockJsBridge).onCloseListener = mockOnCloseListener
    }

    @Test
    fun testSetAppEvent_shouldSetOnAppEventListenerOnJSBridge() {
        val inlineInAppView = InlineInAppView(context)
        val mockAppEventListener: OnAppEventListener = mock()

        inlineInAppView.onAppEventListener = mockAppEventListener

        verify(mockJsBridge).onAppEventListener = mockAppEventListener
    }

    @Test
    fun testOnLoad_shouldSetOnButtonClickedListener_onSuccessfulFetch() {
        val expectedBody = """{"inlineMessages":[
                                |{"campaignId":"7625","html":"<html>Hello World2</html>","viewId":"$VIEW_ID"}],"oldCampaigns":[]}""".trimMargin()
        whenever(mockResponseModel.body).thenReturn(expectedBody)

        val latch = CountDownLatch(1)
        val inlineInAppView = InlineInAppView(context)
        inlineInAppView.onCompletionListener = CompletionListener { latch.countDown() }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        verify(mockJsBridge).onButtonClickedListener = any()
    }

    @Test
    fun testOnButtonClickedTriggered() {
        val inlineInAppView = InlineInAppView(context)
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        ReflectionTestUtils.invokeInstanceMethod<OnButtonClickedListener>(inlineInAppView, "onButtonClickedTriggered", Pair(String::class.java, "campaignId"))
                .invoke("buttonId", JSONObject())

        verify(mockButtonClickedRepository).add(any())
        verify(mockInAppInternal).trackInternalCustomEvent(any(), any(), anyOrNull())
    }
}