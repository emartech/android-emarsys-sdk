package com.emarsys.inapp.ui

import androidx.test.rule.ActivityTestRule
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.mobileengage.iam.jsbridge.*
import com.emarsys.mobileengage.iam.model.InAppMetaData
import com.emarsys.mobileengage.iam.webview.IamWebView
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.ExtensionTestUtils.runOnMain
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.fake.FakeActivity
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
        const val REQUEST_ID = "testRequestId"
    }

    private lateinit var mockWebViewProvider: IamWebViewProvider
    private lateinit var mockIamJsBridgeFactory: IamJsBridgeFactory
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory
    private lateinit var mockIamWebView: IamWebView
    private lateinit var mockJsBridge: IamJsBridge
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockJsCommandFactory: JSCommandFactory
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider

    private lateinit var inlineInAppView: InlineInAppView

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(FakeActivity::class.java)

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockJsBridge = mock()
        mockIamJsBridgeFactory = mock { on { createJsBridge(any()) } doReturn mockJsBridge }
        mockJsCommandFactory = mock()
        mockCurrentActivityProvider = mock {
            on { get() } doReturn activityTestRule.activity
        }

        mockIamWebView = spy(runOnMain {
            IamWebView(
                concurrentHandlerHolder,
                mockIamJsBridgeFactory,
                mockJsCommandFactory,
                mockCurrentActivityProvider
            )
        })

        mockWebViewProvider = mock {
            on { provide() }.thenReturn(mockIamWebView)
        }
        mockRequestModel = mock {
            on { id } doReturn REQUEST_ID
        }
        mockResponseModel = mock {
            on { requestModel } doReturn mockRequestModel
        }
        mockRequestManager = mock()
        mockRequestModelFactory = mock {
            on { createFetchInlineInAppMessagesRequest("testViewId") }.doReturn(mockRequestModel)
        }

        setupEmarsysComponent(
            FakeDependencyContainer(
                webViewProvider = mockWebViewProvider,
                concurrentHandlerHolder = concurrentHandlerHolder,
                requestManager = mockRequestManager,
                mobileEngageRequestModelFactory = mockRequestModelFactory,
            )
        )

        inlineInAppView = InlineInAppView(activityTestRule.activity)

        runOnMain {}
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
        requestManagerRespond(responseBody = expectedBody)

        val latch = CountDownLatch(1)
        inlineInAppView.onCompletionListener = CompletionListener { latch.countDown() }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        val expectedHtml = "<html>Hello World</html>"

        verify(mockIamWebView).load(
            eq(expectedHtml),
            eq(InAppMetaData("765", null, null)),
            any()
        )
    }

    @Test
    fun testFilterMessagesById_whenViewId_isMissing() {
        val expectedBody = JSONObject(
            """{"inlineMessages":[
                                |{"campaignId":"765","html":"<html>Hello World</html>"},
                                |{"campaignId":"7625","html":"<html>Hello World2</html>"}],"oldCampaigns":[]}""".trimMargin()
        )
        requestManagerRespond(responseBody = expectedBody)

        val latch = CountDownLatch(1)
        inlineInAppView.onCompletionListener = CompletionListener { latch.countDown() }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        verify(mockIamWebView, times(0)).load(
            any(),
            any(),
            any()
        )
    }

    @Test
    fun testFilterMessagesById_whenInlineMessages_isMissing() {
        val expectedBody = JSONObject("""{}""".trimMargin())

        requestManagerRespond(responseBody = expectedBody)

        var error: Throwable? = null

        val latch = CountDownLatch(1)
        inlineInAppView.onCompletionListener = CompletionListener {
            error = it
            latch.countDown()
        }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        error shouldNotBe null
        error!!.message shouldBe "Inline In-App HTML content must not be empty, please check your viewId!"
        verify(mockIamWebView, times(0)).load(
            any(),
            any(),
            any()
        )
    }

    @Test
    fun testFilterMessagesById_whenBodyIsEmpty() {
        val expectedBody = JSONObject()
        requestManagerRespond(responseBody = expectedBody)

        val latch = CountDownLatch(1)
        inlineInAppView.onCompletionListener = CompletionListener { latch.countDown() }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        verify(mockIamWebView, times(0)).load(
            any(),
            any(),
            any()
        )
    }

    @Test
    fun testLoadInApp_whenResponseError() {
        val expectedBody = """errorBody""".trimMargin()
        val expectedStatusCode = 500
        val expectedMessage = "Error message"
        requestManagerRespond(false, responseBody = JSONObject())
        whenever(mockResponseModel.body).thenReturn(expectedBody)
        whenever(mockResponseModel.statusCode).thenReturn(expectedStatusCode)
        whenever(mockResponseModel.message).thenReturn(expectedMessage)

        var error: Throwable? = null

        val latch = CountDownLatch(1)
        inlineInAppView.onCompletionListener = CompletionListener {
            error = it
            latch.countDown()
        }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        error shouldNotBe null
        (error as ResponseErrorException).body shouldBe expectedBody
        (error as ResponseErrorException).statusCode shouldBe expectedStatusCode
        (error as ResponseErrorException).statusMessage shouldBe expectedMessage

        verify(mockIamWebView, times(0)).load(
            any(),
            any(),
            any()
        )
    }

    @Test
    fun testLoadInApp_whenException() {
        val expectedException = Exception("Error happened")
        requestManagerRespond(false, exception = expectedException)

        val latch = CountDownLatch(1)
        inlineInAppView.onCompletionListener = CompletionListener {
            it shouldBe expectedException
            latch.countDown()
        }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        verify(mockIamWebView, times(0)).load(
            any(),
            any(),
            any()
        )
    }

    @Test
    fun testSetOnCloseEvent_shouldSetOnCloseListenerOnJSBridge() {
        val expectedBody = JSONObject(
            """{"inlineMessages":[
                                |{"campaignId":"765","html":"<html>Hello World</html>","viewId":"$VIEW_ID"},
                                |{"campaignId":"7625","html":"<html>Hello World2</html>","viewId":"$OTHER_VIEW_ID"}],"oldCampaigns":[]}""".trimMargin()
        )
        requestManagerRespond(responseBody = expectedBody)
        val mockOnCloseListener: OnCloseListener = mock()

        val latch = CountDownLatch(1)
        inlineInAppView.onCloseListener = mockOnCloseListener
        inlineInAppView.onCompletionListener = CompletionListener {
            latch.countDown()
        }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        whenever(mockIamWebView.onCloseTriggered).thenAnswer {
            val closeListener = it.arguments[0]
            closeListener shouldBe mockOnCloseListener
        }

    }

    @Test
    fun testSetAppEvent_shouldSetOnAppEventListenerOnJSBridge() {
        val expectedBody = JSONObject(
            """{"inlineMessages":[
                                |{"campaignId":"765","html":"<html>Hello World</html>","viewId":"$VIEW_ID"},
                                |{"campaignId":"7625","html":"<html>Hello World2</html>","viewId":"$OTHER_VIEW_ID"}],"oldCampaigns":[]}""".trimMargin()
        )
        requestManagerRespond(responseBody = expectedBody)
        val mockAppEventListener: OnAppEventListener = mock()

        val latch = CountDownLatch(1)
        inlineInAppView.onAppEventListener = mockAppEventListener
        inlineInAppView.onCompletionListener = CompletionListener {
            latch.countDown()
        }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        whenever(mockIamWebView.onAppEventTriggered).thenAnswer {
            val appEventListener = it.arguments[0]
            appEventListener shouldBe mockAppEventListener
        }
    }

    private fun requestManagerRespond(
        success: Boolean = true,
        responseBody: JSONObject? = null,
        exception: Exception? = null
    ) {
        whenever(mockRequestManager.submitNow(any(), any())).thenAnswer {
            val coreCompletionHandler: CoreCompletionHandler =
                (it.arguments[1] as CoreCompletionHandler)
            if (responseBody != null) {
                whenever(mockResponseModel.parsedBody).thenReturn(responseBody)
                if (success) {
                    coreCompletionHandler.onSuccess(REQUEST_ID, mockResponseModel)
                } else {
                    coreCompletionHandler.onError(REQUEST_ID, mockResponseModel)
                }
            } else if (exception != null) {
                coreCompletionHandler.onError(REQUEST_ID, exception)
            }
        }
    }
}