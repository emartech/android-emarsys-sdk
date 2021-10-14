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
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.factory.ScopeDelegatorCompletionHandlerProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.di.tearDownEmarsysComponent
import com.emarsys.fake.FakeRestClient
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.inline.InlineInAppWebViewFactory
import com.emarsys.mobileengage.iam.jsbridge.*
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.spy
import org.mockito.kotlin.*
import java.util.concurrent.CountDownLatch

class InlineInAppViewTest {
    private companion object {
        const val VIEW_ID = "testViewId"
        const val OTHER_VIEW_ID = "testViewId2"
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
    private lateinit var mockScopeDelegatorCompletionHandlerProvider: ScopeDelegatorCompletionHandlerProvider

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        var onMessageLoadedListener: MessageLoadedListener? = null
        context = InstrumentationRegistry.getTargetContext()
        webView = mock {
            on { layoutParams } doReturn ViewGroup.LayoutParams(10, 10)
            on {
                loadDataWithBaseURL(
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull()
                )
            } doAnswer {
                onMessageLoadedListener?.onMessageLoaded()
            }
        }
        mockInlineInAppWebViewFactory = mock {
            on { create(any()) } doAnswer {
                onMessageLoadedListener = it.arguments[0] as MessageLoadedListener
                webView
            }
        }
        mockJsBridge = mock()
        mockIamJsBridgeFactory = mock { on { createJsBridge(any(), any()) } doReturn mockJsBridge }
        mockRequestModel = mock {
            on { id } doReturn "requestId"
        }
        mockResponseModel = mock {
            on { requestModel } doReturn mockRequestModel
        }
        mockScopeDelegatorCompletionHandlerProvider = mock {
            on { provide(any(), any()) } doAnswer {
                it.arguments[0] as CoreCompletionHandler
            }
            on { provide(any(), any()) } doAnswer {
                it.arguments[0] as CoreCompletionHandler
            }
        }
        mockProvider = mock {
            on { provideProxy(isNull(), any()) } doAnswer {
                it.arguments[1] as CoreCompletionHandler
            }
            on { provideProxy(any(), any()) } doAnswer {
                it.arguments[1] as CoreCompletionHandler
            }
        }
        mockRequestManager = spy(
            RequestManager(
                CoreSdkHandlerProvider().provideHandler(),
                mock(),
                mock(),
                mock(),
                FakeRestClient(mockResponseModel, FakeRestClient.Mode.SUCCESS),
                mock(),
                mock(),
                mockProvider,
                mockScopeDelegatorCompletionHandlerProvider,
                mock()
            )
        )
        mockRequestModelFactory = mock {
            on { createFetchInlineInAppMessagesRequest("testViewId") }.doReturn(mockRequestModel)
        }
        mockButtonClickedRepository = mock()
        mockInAppInternal = mock()

        setupEmarsysComponent(
            FakeDependencyContainer(
                inlineInAppWebViewFactory = mockInlineInAppWebViewFactory,
                iamJsBridgeFactory = mockIamJsBridgeFactory,
                requestManager = mockRequestManager,
                mobileEngageRequestModelFactory = mockRequestModelFactory,
                inAppInternal = mockInAppInternal,
                buttonClickedRepository = mockButtonClickedRepository
            )
        )
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testFilterMessagesById() {
        val expectedBody = JSONObject(
            """{"inlineMessages":[
                                |{"campaignId":"765","html":"<html>Hello World</html>","viewId":"$VIEW_ID"},
                                |{"campaignId":"7625","html":"<html>Hello World2</html>","viewId":"$OTHER_VIEW_ID"}],"oldCampaigns":[]}""".trimMargin()
        )
        whenever(mockResponseModel.parsedBody).thenReturn(expectedBody)

        val latch = CountDownLatch(1)
        val inlineInAppView = InlineInAppView(context)
        inlineInAppView.onCompletionListener = CompletionListener { latch.countDown() }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()
        val expectedHtml = "<html>Hello World</html>"

        verify(webView).loadDataWithBaseURL(
            null,
            expectedHtml,
            "text/html; charset=utf-8",
            "UTF-8",
            null
        )
    }

    @Test
    fun testFilterMessagesById_whenViewId_isMissing() {
        val expectedBody = JSONObject(
            """{"inlineMessages":[
                                |{"campaignId":"765","html":"<html>Hello World</html>"},
                                |{"campaignId":"7625","html":"<html>Hello World2</html>"}],"oldCampaigns":[]}""".trimMargin()
        )
        whenever(mockResponseModel.parsedBody).thenReturn(expectedBody)

        val latch = CountDownLatch(1)
        val inlineInAppView = InlineInAppView(context)
        inlineInAppView.onCompletionListener = CompletionListener { latch.countDown() }
        inlineInAppView.loadInApp(VIEW_ID)

        latch.await()

        verify(webView, times(0)).loadDataWithBaseURL(
            any(),
            any(),
            eq("text/html; charset=utf-8"),
            eq("UTF-8"),
            isNull()
        )
    }

    @Test
    fun testFilterMessagesById_whenInlineMessages_isMissing() {
        val expectedBody = JSONObject("""{}""".trimMargin())
        whenever(mockResponseModel.parsedBody).thenReturn(expectedBody)
        var error: Throwable? = null
        val latch = CountDownLatch(1)
        val inlineInAppView = InlineInAppView(context)
        inlineInAppView.onCompletionListener = CompletionListener {
            error = it
            latch.countDown()
        }
        inlineInAppView.loadInApp(VIEW_ID)

        latch.await()
        error shouldNotBe null
        error!!.message shouldBe "Inline In-App HTML content must not be empty, please check your viewId!"
        verify(webView, times(0)).loadDataWithBaseURL(
            any(),
            any(),
            eq("text/html; charset=utf-8"),
            eq("UTF-8"),
            isNull()
        )

    }

    @Test
    fun testFilterMessagesById_whenBodyIsEmpty() {
        val expectedBody: JSONObject? = null
        whenever(mockResponseModel.parsedBody).thenReturn(expectedBody)

        val latch = CountDownLatch(1)
        val inlineInAppView = InlineInAppView(context)
        inlineInAppView.onCompletionListener = CompletionListener { latch.countDown() }
        inlineInAppView.loadInApp(VIEW_ID)

        latch.await()

        verify(webView, times(0)).loadDataWithBaseURL(
            any(),
            any(),
            eq("text/html; charset=utf-8"),
            eq("UTF-8"),
            isNull()
        )

    }

    @Test
    fun testFilterMessagesById_whenWebViewCanNotBeCreated() {
        val expectedBody = JSONObject(
            """{"inlineMessages":[
                                |{"campaignId":"765","html":"<html>Hello World</html>"},
                                |{"campaignId":"7625","html":"<html>Hello World2</html>"}],"oldCampaigns":[]}""".trimMargin()
        )
        whenever(mockInlineInAppWebViewFactory.create(any())).thenReturn(null)
        whenever(mockResponseModel.parsedBody).thenReturn(expectedBody)
        var error: Throwable? = null
        val latch = CountDownLatch(1)
        val inlineInAppView = InlineInAppView(context)
        inlineInAppView.onCompletionListener = CompletionListener {
            error = it
            latch.countDown()
        }
        inlineInAppView.loadInApp(VIEW_ID)

        latch.await()
        error shouldNotBe null
        error!!.message shouldBe "WebView can not be created, please try again later!"
        verify(webView, times(0)).loadDataWithBaseURL(
            any(),
            any(),
            eq("text/html; charset=utf-8"),
            eq("UTF-8"),
            isNull()
        )
    }

    @Test
    fun testLoadInApp_whenResponseError() {
        tearDownEmarsysComponent()
        val expectedBody = """errorBody""".trimMargin()
        val expectedStatusCode = 500
        val expectedMessage = "Error message"
        whenever(mockResponseModel.body).thenReturn(expectedBody)
        whenever(mockResponseModel.message).thenReturn(expectedMessage)
        whenever(mockResponseModel.statusCode).thenReturn(expectedStatusCode)
        mockRequestManager = spy(
            RequestManager(
                CoreSdkHandlerProvider().provideHandler(),
                mock(),
                mock(),
                mock(),
                FakeRestClient(mockResponseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL),
                mock(),
                mock(),
                mockProvider,
                mockScopeDelegatorCompletionHandlerProvider,
                mock()
            )
        )
        setupEmarsysComponent(
            FakeDependencyContainer(
                inlineInAppWebViewFactory = mockInlineInAppWebViewFactory,
                iamJsBridgeFactory = mockIamJsBridgeFactory,
                requestManager = mockRequestManager,
                mobileEngageRequestModelFactory = mockRequestModelFactory
            )
        )
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

        verify(webView, times(0)).loadDataWithBaseURL(
            any(),
            any(),
            eq("text/html; charset=utf-8"),
            eq("UTF-8"),
            isNull()
        )

    }

    @Test
    fun testLoadInApp_whenException() {
        tearDownEmarsysComponent()
        val expectedException = Exception("Error happened")
        mockRequestManager = spy(
            RequestManager(
                CoreSdkHandlerProvider().provideHandler(),
                mock(),
                mock(),
                mock(),
                FakeRestClient(expectedException),
                mock(),
                mock(),
                mockProvider,
                mockScopeDelegatorCompletionHandlerProvider,
                mock()
            )
        )
        setupEmarsysComponent(
            FakeDependencyContainer(
                inlineInAppWebViewFactory = mockInlineInAppWebViewFactory,
                iamJsBridgeFactory = mockIamJsBridgeFactory,
                requestManager = mockRequestManager,
                mobileEngageRequestModelFactory = mockRequestModelFactory
            )
        )
        val inlineInAppView = InlineInAppView(context)
        val latch = CountDownLatch(1)
        inlineInAppView.onCompletionListener = CompletionListener { error ->
            error shouldBe expectedException
            latch.countDown()
        }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        verify(webView, times(0)).loadDataWithBaseURL(
            any(),
            any(),
            eq("text/html; charset=utf-8"),
            eq("UTF-8"),
            isNull()
        )

    }

    @Test
    fun testSetOnCloseEvent_shouldSetOnCloseListenerOnJSBridge() {
        val expectedBody = JSONObject(
            """{"inlineMessages":[
                                |{"campaignId":"765","html":"<html>Hello World</html>","viewId":"$VIEW_ID"},
                                |{"campaignId":"7625","html":"<html>Hello World2</html>","viewId":"$OTHER_VIEW_ID"}],"oldCampaigns":[]}""".trimMargin()
        )
        whenever(mockResponseModel.parsedBody).thenReturn(expectedBody)
        val latch = CountDownLatch(1)
        val inlineInAppView = InlineInAppView(context)
        val mockOnCloseListener: OnCloseListener = mock()

        inlineInAppView.onCloseListener = mockOnCloseListener
        inlineInAppView.onCompletionListener = CompletionListener {
            latch.countDown()
        }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        argumentCaptor<JSCommandFactory>().apply {
            verify(mockIamJsBridgeFactory).createJsBridge(capture(), any())

            val onCloseListener = ReflectionTestUtils.getInstanceField<OnCloseListener>(
                firstValue,
                "onCloseTriggered"
            )
            onCloseListener shouldBe mockOnCloseListener
        }
    }

    @Test
    fun testSetAppEvent_shouldSetOnAppEventListenerOnJSBridge() {
        val expectedBody = JSONObject(
            """{"inlineMessages":[
                                |{"campaignId":"765","html":"<html>Hello World</html>","viewId":"$VIEW_ID"},
                                |{"campaignId":"7625","html":"<html>Hello World2</html>","viewId":"$OTHER_VIEW_ID"}],"oldCampaigns":[]}""".trimMargin()
        )
        whenever(mockResponseModel.parsedBody).thenReturn(expectedBody)
        val latch = CountDownLatch(1)
        val inlineInAppView = InlineInAppView(context)
        val mockAppEventListener: OnAppEventListener = mock()

        inlineInAppView.onAppEventListener = mockAppEventListener
        inlineInAppView.onCompletionListener = CompletionListener {
            latch.countDown()
        }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        argumentCaptor<JSCommandFactory>().apply {
            verify(mockIamJsBridgeFactory).createJsBridge(capture(), any())

            val onAppEventListener = ReflectionTestUtils.getInstanceField<OnAppEventListener>(
                firstValue,
                "onAppEventTriggered"
            )
            onAppEventListener shouldBe mockAppEventListener
        }
    }
}