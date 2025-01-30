package com.emarsys.inapp.ui


import androidx.test.core.app.ActivityScenario
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactory
import com.emarsys.mobileengage.iam.jsbridge.OnAppEventListener
import com.emarsys.mobileengage.iam.jsbridge.OnCloseListener
import com.emarsys.mobileengage.iam.model.InAppMetaData
import com.emarsys.mobileengage.iam.webview.IamWebView
import com.emarsys.mobileengage.iam.webview.IamWebViewFactory
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.ExtensionTestUtils.runOnMain
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.fake.FakeActivity
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class InlineInAppViewTest {
    private companion object {
        const val VIEW_ID = "testViewId"
        const val OTHER_VIEW_ID = "testViewId2"
        const val REQUEST_ID = "testRequestId"
    }

    private lateinit var mockWebViewFactory: IamWebViewFactory
    private lateinit var mockIamJsBridgeFactory: IamJsBridgeFactory
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory
    private lateinit var mockIamWebView: IamWebView
    private lateinit var mockJsBridge: IamJsBridge
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockJsCommandFactory: JSCommandFactory
    private lateinit var inlineInAppView: InlineInAppView
    private lateinit var scenario: ActivityScenario<FakeActivity>


    @Before
    fun setUp() {

        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockJsBridge = mockk(relaxed = true)
        mockIamJsBridgeFactory =
            mockk(relaxed = true) { every { createJsBridge(any()) } returns mockJsBridge }
        mockJsCommandFactory = mockk(relaxed = true)
        scenario = ActivityScenario.launch(FakeActivity::class.java)
        scenario.onActivity { activity ->
            mockIamWebView = spyk(runOnMain {
                IamWebView(
                    concurrentHandlerHolder,
                    mockIamJsBridgeFactory,
                    mockJsCommandFactory,
                    activity
                )
            })

            mockWebViewFactory = mockk(relaxed = true) {
                every { create(activity) } returns (mockIamWebView)
            }
            mockRequestModel = mockk(relaxed = true) {
                every { id } returns REQUEST_ID
            }
            mockResponseModel = mockk(relaxed = true) {
                every { requestModel } returns mockRequestModel
            }
            mockRequestManager = mockk(relaxed = true)
            mockRequestModelFactory = mockk(relaxed = true) {
                every { createFetchInlineInAppMessagesRequest("testViewId") }.returns(
                    mockRequestModel
                )
            }

            setupEmarsysComponent(
                FakeDependencyContainer(
                    webViewFactory = mockWebViewFactory,
                    concurrentHandlerHolder = concurrentHandlerHolder,
                    requestManager = mockRequestManager,
                    mobileEngageRequestModelFactory = mockRequestModelFactory,
                )
            )

            inlineInAppView = InlineInAppView(activity)
        }
        runOnMain {}
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
        scenario.close()
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

        verify {
            mockIamWebView.load(
                (expectedHtml),
                (InAppMetaData("765", null, null)),
                any()
            )
        }
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

        verify(exactly = 0) {
            mockIamWebView.load(
                any(),
                any(),
                any()
            )
        }
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
        verify(exactly = 0) {
            mockIamWebView.load(
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun testFilterMessagesById_whenBodyIsEmpty() {
        val expectedBody = JSONObject()
        requestManagerRespond(responseBody = expectedBody)

        val latch = CountDownLatch(1)
        inlineInAppView.onCompletionListener = CompletionListener { latch.countDown() }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        verify(exactly = 0) {
            mockIamWebView.load(
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun testLoadInApp_whenResponseError() {
        val expectedBody = """errorBody""".trimMargin()
        val expectedStatusCode = 500
        val expectedMessage = "Error message"
        requestManagerRespond(false, responseBody = JSONObject())
        every { mockResponseModel.body } returns expectedBody
        every { mockResponseModel.statusCode } returns expectedStatusCode
        every { mockResponseModel.message } returns expectedMessage

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

        verify(exactly = 0) {
            mockIamWebView.load(
                any(),
                any(),
                any()
            )
        }
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

        verify(exactly = 0) {
            mockIamWebView.load(
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun testSetOnCloseEvent_shouldSetOnCloseListenerOnJSBridge() {
        val expectedBody = JSONObject(
            """{"inlineMessages":[
                                |{"campaignId":"765","html":"<html>Hello World</html>","viewId":"$VIEW_ID"},
                                |{"campaignId":"7625","html":"<html>Hello World2</html>","viewId":"$OTHER_VIEW_ID"}],"oldCampaigns":[]}""".trimMargin()
        )
        requestManagerRespond(responseBody = expectedBody)
        val mockOnCloseListener: OnCloseListener = mockk(relaxed = true)

        val latch = CountDownLatch(1)
        inlineInAppView.onCloseListener = mockOnCloseListener
        inlineInAppView.onCompletionListener = CompletionListener {
            latch.countDown()
        }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        every {
            mockIamWebView.onCloseTriggered
        } answers {
            val closeListener = it.invocation.args[0]
            closeListener shouldBe mockOnCloseListener
            null
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
        val mockAppEventListener: OnAppEventListener = mockk(relaxed = true)

        val latch = CountDownLatch(1)
        inlineInAppView.onAppEventListener = mockAppEventListener
        inlineInAppView.onCompletionListener = CompletionListener {
            latch.countDown()
        }
        inlineInAppView.loadInApp(VIEW_ID)
        latch.await()

        every {
            mockIamWebView.onAppEventTriggered
        } answers {
            val appEventListener = it.invocation.args[0]
            appEventListener shouldBe mockAppEventListener
            null
        }
    }

    private fun requestManagerRespond(
        success: Boolean = true,
        responseBody: JSONObject? = null,
        exception: Exception? = null
    ) {
        every {
            mockRequestManager.submitNow(any(), any())
        } answers {
            val coreCompletionHandler: CoreCompletionHandler =
                (it.invocation.args[1] as CoreCompletionHandler)
            if (responseBody != null) {
                every { mockResponseModel.parsedBody } returns responseBody
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