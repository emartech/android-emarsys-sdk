package com.emarsys.mobileengage.iam.dialog

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.fragment.app.testing.EmptyFragmentActivity
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.util.log.entry.InAppLoadingTime
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactoryProvider
import com.emarsys.mobileengage.iam.model.InAppMetaData
import com.emarsys.mobileengage.iam.webview.IamWebView
import com.emarsys.mobileengage.iam.webview.IamWebViewFactory
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import com.emarsys.testUtil.ExtensionTestUtils.runOnMain
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.ReflectionTestUtils
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class IamDialogTest {
    private companion object {
        const val CAMPAIGN_ID = "id_value"
        const val SID = "testSid"
        const val URL = "https://www.emarsys.com"
        const val ON_SCREEN_TIME_KEY = "on_screen_time"
        const val CAMPAIGN_ID_KEY = "id"
        const val REQUEST_ID_KEY = "request_id"
    }

    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockWebViewFactory: IamWebViewFactory
    private lateinit var mockJSCommandFactoryProvider: JSCommandFactoryProvider
    private lateinit var mockJSCommandFactory: JSCommandFactory
    private lateinit var mockJsBridgeFactory: IamJsBridgeFactory
    private lateinit var mockJsBridge: IamJsBridge
    private lateinit var mockConcurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider

    private lateinit var iamDialog: IamDialog
    private var scenario: ActivityScenario<EmptyFragmentActivity>? = null
    private lateinit var iamWebView: IamWebView

    @Before
    fun setUp() {
        mockTimestampProvider = mockk(relaxed = true)
        val mockUuidProvider: UUIDProvider = mockk(relaxed = true)
        every {
            mockUuidProvider.provideId()
        } returns "uuid"

        mockJsBridge = mockk(relaxed = true)
        mockJsBridgeFactory = mockk(relaxed = true)
        every {
            mockJsBridgeFactory.createJsBridge(any())
        } returns mockJsBridge

        mockJSCommandFactory = mockk(relaxed = true)
        mockJSCommandFactoryProvider = mockk(relaxed = true)
        every {
            mockJSCommandFactoryProvider.provide()
        } returns mockJSCommandFactory

        mockConcurrentHandlerHolder = mockk(relaxed = true)
        mockWebViewFactory = mockk(relaxed = true)
        mockCurrentActivityProvider = mockk(relaxed = true)
        setupMobileEngageComponent(
            FakeMobileEngageDependencyContainer(
                timestampProvider = mockTimestampProvider,
                uuidProvider = mockUuidProvider,
                webViewFactory = mockWebViewFactory,
                jsCommandFactoryProvider = mockJSCommandFactoryProvider,
                iamJsBridgeFactory = mockJsBridgeFactory,
                concurrentHandlerHolder = mockConcurrentHandlerHolder,
                currentActivityProvider = mockCurrentActivityProvider
            )
        )
        iamDialog = IamDialog(mockTimestampProvider, mockWebViewFactory)
    }

    @After
    fun tearDown() {
        tearDownMobileEngageComponent()
        scenario?.close()
    }

    private fun launchActivityIfNeeded() {
        if (scenario == null) {
            scenario = ActivityScenario.launch(EmptyFragmentActivity::class.java)
            val countDownLatch = CountDownLatch(1)
            scenario!!.onActivity { activity ->
                every {
                    mockCurrentActivityProvider.get()
                } returns activity

                iamWebView = createWebView()
                countDownLatch.countDown()
            }
            countDownLatch.await()
        }
    }

    @Test
    fun testConstructor_forTimestampProvider() {
        val dialogTimestampProvider =
            ReflectionTestUtils.getInstanceField<TimestampProvider>(iamDialog, "timestampProvider")

        dialogTimestampProvider shouldBe mockTimestampProvider
    }

    @Test
    fun testCreate_shouldReturnIamDialogInstance() {
        val defaultIamDialog = IamDialog()

        defaultIamDialog shouldNotBe null
        iamDialog shouldNotBe null
        iamDialog::class.java shouldBe IamDialog::class.java
    }

    @Test
    fun testCreate_shouldInitializeDialog_withCampaignId() {
        val campaignId = "123456789"
        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, campaignId)
        bundle.putString(IamDialog.SID, null)
        bundle.putString(IamDialog.URL, null)
        bundle.putString(IamDialog.REQUEST_ID, null)

        iamDialog.arguments = bundle

        val result = iamDialog.arguments

        result shouldNotBe null
        result!!.getString(CAMPAIGN_ID_KEY) shouldBe campaignId
    }

    @Test
    fun testCreate_shouldInitializeDialog_withRequestId() {
        val requestId = "requestId"
        val campaignId = "campaignId"
        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, campaignId)
        bundle.putString(IamDialog.SID, null)
        bundle.putString(IamDialog.URL, null)
        bundle.putString(IamDialog.REQUEST_ID, requestId)

        iamDialog.arguments = bundle

        val result = iamDialog.arguments

        result shouldNotBe null
        result!!.getString(REQUEST_ID_KEY) shouldBe requestId
    }

    @Test
    fun testCreate_shouldInitializeDialog_withSid() {
        val requestId = "requestId"
        val campaignId = "campaignId"
        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, campaignId)
        bundle.putString(IamDialog.SID, SID)
        bundle.putString(IamDialog.URL, null)
        bundle.putString(IamDialog.REQUEST_ID, requestId)

        iamDialog.arguments = bundle

        val result = iamDialog.arguments

        result shouldNotBe null
        result!!.getString("sid") shouldBe "testSid"
    }

    @Test
    fun testCreate_shouldInitializeDialog_withUrl() {
        val requestId = "requestId"
        val campaignId = "campaignId"
        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, campaignId)
        bundle.putString(IamDialog.SID, SID)
        bundle.putString(IamDialog.URL, URL)
        bundle.putString(IamDialog.REQUEST_ID, requestId)

        iamDialog.arguments = bundle

        val result = iamDialog.arguments

        result shouldNotBe null
        result!!.getString("url") shouldBe "https://www.emarsys.com"
    }

    @Test
    fun testOnCreate_shouldCreateWebView_withFragmentActivity_whenActivityReferenceIsNull() {
        val fragmentScenario = launchFragment(initialState = Lifecycle.State.CREATED) {
            iamDialog
        }
        fragmentScenario.onFragment { fragment ->
            verify { mockWebViewFactory.create(fragment.activity!!) }
        }
    }

    @Test
    fun testOnCreate_should_notOverrideHtml_whenItIsStillAvailable() {
        val mockIamWebView: IamWebView = mockk(relaxed = true)
        every { mockWebViewFactory.create(any()) } returns mockIamWebView

        val testHtml = "<html>123</html>"
        val testInAppMetaData = InAppMetaData("123", null, null)
        ReflectionTestUtils.setInstanceField(iamDialog, "html", testHtml)
        ReflectionTestUtils.setInstanceField(iamDialog, "inAppMetaData", testInAppMetaData)

        val fragmentScenario = launchFragment(initialState = Lifecycle.State.CREATED) {
            iamDialog
        }

        fragmentScenario.onFragment {
            verify { mockIamWebView.load(testHtml, testInAppMetaData, any()) }
        }
    }

    @Test
    fun testCreate_shouldInitializeDialog_withOutRequestId() {
        val campaignId = "campaignId"
        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, campaignId)
        bundle.putString(IamDialog.SID, SID)
        bundle.putString(IamDialog.URL, URL)
        bundle.putString(IamDialog.REQUEST_ID, null)

        iamDialog.arguments = bundle

        val result = iamDialog.arguments

        result shouldNotBe null
        result!!.getString(REQUEST_ID_KEY) shouldBe null
    }

    @Test
    fun testInitialization_setsDimAmountToZero() {
        launchActivityIfNeeded()

        every { mockWebViewFactory.create(any()) } returns iamWebView

        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        bundle.putString(IamDialog.SID, SID)
        bundle.putString(IamDialog.URL, URL)
        bundle.putString(IamDialog.REQUEST_ID, null)
        val fragmentScenario = launchFragment(bundle, initialState = Lifecycle.State.CREATED) {
            iamDialog
        }
        displayDialog(fragmentScenario)
        fragmentScenario.onFragment {
            val expected = 0.0f
            val actual = it.dialog!!.window!!.attributes.dimAmount
            org.junit.Assert.assertEquals(expected.toDouble(), actual.toDouble(), 0.0000001)
        }
    }

    @Test
    fun testInitialization_setsDialogToFullscreen() {
        launchActivityIfNeeded()

        every { mockWebViewFactory.create(any()) } returns iamWebView

        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        bundle.putString(IamDialog.SID, SID)
        bundle.putString(IamDialog.URL, URL)
        bundle.putString(IamDialog.REQUEST_ID, null)

        val fragmentScenario = launchFragment(bundle, initialState = Lifecycle.State.CREATED) {
            iamDialog
        }

        fragmentScenario.onFragment {
            fragmentScenario.moveToState(Lifecycle.State.STARTED)
            fragmentScenario.moveToState(Lifecycle.State.RESUMED)

            val windowWidth = it.activity!!.window.decorView.measuredWidth
            val windowHeight = it.activity!!.window.decorView.measuredHeight

            val dialogWidth = it.view!!.layoutParams.width
            val dialogHeight = it.view!!.layoutParams.height

            windowHeight shouldBeGreaterThan 0
            windowWidth shouldBeGreaterThan 0
            dialogHeight shouldBe -1
            dialogWidth shouldBe -1
        }
    }

    @Test
    fun testDialog_stillVisible_afterOrientationChange() {
        launchActivityIfNeeded()

        every { mockWebViewFactory.create(any()) } returns iamWebView

        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        bundle.putString(IamDialog.SID, SID)
        bundle.putString(IamDialog.URL, URL)
        bundle.putString(IamDialog.REQUEST_ID, REQUEST_ID_KEY)
        bundle.putSerializable("loading_time", InAppLoadingTime(0, 0))

        val fragmentScenario = launchFragment(bundle, initialState = Lifecycle.State.CREATED) {
            iamDialog
        }

        fragmentScenario.onFragment {
            fragmentScenario.moveToState(Lifecycle.State.STARTED)
            fragmentScenario.moveToState(Lifecycle.State.RESUMED)

            it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            val windowWidth = it.activity!!.window.decorView.measuredWidth
            val windowHeight = it.activity!!.window.decorView.measuredHeight

            val dialogWidth = it.view!!.layoutParams.width
            val dialogHeight = it.view!!.layoutParams.height

            windowHeight shouldBeGreaterThan 0
            windowWidth shouldBeGreaterThan 0
            dialogHeight shouldBe -1
            dialogWidth shouldBe -1
        }
    }

    @Test
    fun testOnResume_callsActions_ifProvided() {
        val args = Bundle()
        args.putString(CAMPAIGN_ID_KEY, "123456789")
        val actions: List<OnDialogShownAction> =
            listOf(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true))

        iamDialog.apply {
            setActions(actions)
            arguments = args
        }

        iamDialog.onResume()

        for (action in actions) {
            verify { (action).execute("123456789", null, null) }
        }
    }

    @Test
    fun testOnResume_callsActions_onlyOnce() {
        val actions: List<OnDialogShownAction> =
            listOf(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true))

        val args = Bundle()
        args.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        args.putString(IamDialog.SID, SID)
        args.putString(IamDialog.URL, URL)

        iamDialog.apply {
            setActions(actions)
            arguments = args
        }
        iamDialog.onResume()
        iamDialog.onResume()

        iamDialog.loadInApp(
            "<html></html>",
            InAppMetaData("123456789", null, null),
            MessageLoadedListener {},
            mockk(relaxed = true)
        )

        for (action in actions) {
            verify(exactly = 1) { action.execute(any(), any(), any()) }
        }
    }

    @Test
    fun testOnScreenTime_savesDuration_betweenResumeAndPause() {
        every { mockTimestampProvider.provideTimestamp() } returns 100L andThen 250L
        val args = Bundle()
        args.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        args.putString(IamDialog.SID, SID)
        args.putString(IamDialog.URL, URL)

        iamDialog.arguments = args
        iamDialog.onResume()

        iamDialog.loadInApp(
            "<html></html>",
            InAppMetaData("123456789", null, null),
            MessageLoadedListener {},
            mockk(relaxed = true)
        )

        iamDialog.onPause()

        val onScreenTime = iamDialog.arguments?.getLong(ON_SCREEN_TIME_KEY) ?: -1

        iamDialog.arguments shouldNotBe null
        onScreenTime shouldBe 150
    }

    @Test
    fun testOnScreenTime_aggregatesDurations_betweenMultipleResumeAndPause() {
        every { mockTimestampProvider.provideTimestamp() } returnsMany listOf(
            100,
            250L,
            1000L,
            1003
        )

        val args = Bundle()
        args.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        args.putString(IamDialog.SID, SID)
        args.putString(IamDialog.URL, URL)
        iamDialog.arguments = args
        iamDialog.onResume()

        iamDialog.loadInApp(
            "<html></html>",
            InAppMetaData("123456789", null, null),
            {},
            mockk(relaxed = true)
        )

        iamDialog.onPause()
        iamDialog.arguments shouldNotBe null
        iamDialog.arguments!!.getLong(ON_SCREEN_TIME_KEY) shouldBe 150

        iamDialog.onResume()
        iamDialog.onPause()

        iamDialog.arguments shouldNotBe null
        iamDialog.arguments!!.getLong(ON_SCREEN_TIME_KEY) shouldBe 153

    }

    @Test
    fun testOnStart_shouldNotThrowTheSpecifiedChildAlreadyHasAParent_exception() {
        launchActivityIfNeeded()

        every { mockWebViewFactory.create(any()) } returns iamWebView

        var result: Exception? = null
        try {
            val fragmentScenario = launchFragment(initialState = Lifecycle.State.CREATED) {
                iamDialog
            }
            displayDialog(fragmentScenario)
            fragmentScenario.onFragment {
                it.activity?.runOnUiThread { it.onStart() }
            }
        } catch (exception: IllegalStateException) {
            result = exception
        }

        result shouldBe null
    }

    @Test
    fun testOnStart_shouldNotThrowTheSpecifiedWebViewAlreadyHasAParent_exception() {
        launchActivityIfNeeded()

        every { mockWebViewFactory.create(any()) } returns iamWebView

        var result: Exception? = null
        try {

            runOnMain {
                val webView = WebView(getTargetContext())
                LinearLayout(getTargetContext()).addView(webView)

                iamWebView.webView = webView
            }

            every { mockWebViewFactory.create(any()) } returns iamWebView

            val fragmentScenario = launchFragment(initialState = Lifecycle.State.CREATED) {
                iamDialog
            }
            displayDialog(fragmentScenario)
            fragmentScenario.onFragment {
                it.activity?.runOnUiThread { it.onStart() }
            }
        } catch (exception: IllegalStateException) {
            result = exception
        }
        result shouldBe null
    }

    @Test
    fun testLoadInApp_shouldNotTriggerWebViewLoad_beforeOnStart() {
        val html = "<html></html>"
        val inAppMetaData = InAppMetaData(CAMPAIGN_ID, null, null)
        val messageLoadedListener = MessageLoadedListener { }

        val mockIamWebView: IamWebView = mockk(relaxed = true)
        every { mockWebViewFactory.create(mockk(relaxed = true)) } returns mockIamWebView

        val dialog = IamDialog(
            mobileEngage().timestampProvider,
            mockWebViewFactory
        )

        ReflectionTestUtils.setInstanceField(dialog, "iamWebView", mockIamWebView)

        dialog.loadInApp(html, inAppMetaData, messageLoadedListener, mockk(relaxed = true))

        verify(exactly = 0) { mockIamWebView.load(any(), any(), any()) }
    }

    @Test
    fun testOnStart_shouldTriggerWebViewLoad_withStagedHtmlAndListener() {
        launchActivityIfNeeded()

        val mockIamWebView: IamWebView = mockk(relaxed = true)
        every { mockWebViewFactory.create(any()) } returns mockIamWebView
        every { mockIamWebView.webView } returns WebView(getTargetContext())

        val html = "<html></html>"
        val inAppMetaData = InAppMetaData(CAMPAIGN_ID, null, null)
        val messageLoadedListener = MessageLoadedListener { }

        iamDialog.loadInApp(html, inAppMetaData, messageLoadedListener, mockk(relaxed = true))

        val fragmentScenario = launchFragment(initialState = Lifecycle.State.CREATED) { iamDialog }
        displayDialog(fragmentScenario)

        verify { mockIamWebView.load(html, inAppMetaData, messageLoadedListener) }
    }

    @Test
    fun testOnStart_shouldTriggerWebViewLoad_onlyOnce_acrossMultipleStartStop() {
        launchActivityIfNeeded()

        val mockIamWebView: IamWebView = mockk(relaxed = true)
        every { mockWebViewFactory.create(any()) } returns mockIamWebView
        every { mockIamWebView.webView } returns WebView(getTargetContext())

        val html = "<html></html>"
        val inAppMetaData = InAppMetaData(CAMPAIGN_ID, null, null)
        val messageLoadedListener = MessageLoadedListener { }

        iamDialog.loadInApp(html, inAppMetaData, messageLoadedListener, mockk(relaxed = true))

        val fragmentScenario = launchFragment(initialState = Lifecycle.State.CREATED) { iamDialog }
        displayDialog(fragmentScenario)
        fragmentScenario.moveToState(Lifecycle.State.CREATED)
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        verify(exactly = 1) { mockIamWebView.load(html, inAppMetaData, messageLoadedListener) }
    }

    @Test
    fun testTwoSequentialDialogs_bothTriggerWebViewLoad() {
        launchActivityIfNeeded()

        val firstWebView: IamWebView = mockk(relaxed = true)
        val secondWebView: IamWebView = mockk(relaxed = true)
        every { firstWebView.webView } returns WebView(getTargetContext())
        every { secondWebView.webView } returns WebView(getTargetContext())
        every { mockWebViewFactory.create(any()) } returnsMany listOf(firstWebView, secondWebView)

        val firstHtml = "<html><body>first</body></html>"
        val secondHtml = "<html><body>second</body></html>"
        val firstMeta = InAppMetaData("campaign-1", null, null)
        val secondMeta = InAppMetaData("campaign-2", null, null)

        val firstDialog = IamDialog(mockTimestampProvider, mockWebViewFactory)
        firstDialog.loadInApp(firstHtml, firstMeta, MessageLoadedListener {}, mockk(relaxed = true))
        val firstScenario = launchFragment(initialState = Lifecycle.State.CREATED) { firstDialog }
        displayDialog(firstScenario)

        val secondDialog = IamDialog(mockTimestampProvider, mockWebViewFactory)
        secondDialog.loadInApp(secondHtml, secondMeta, MessageLoadedListener {}, mockk(relaxed = true))
        val secondScenario = launchFragment(initialState = Lifecycle.State.CREATED) { secondDialog }
        displayDialog(secondScenario)

        verify { firstWebView.load(firstHtml, firstMeta, any()) }
        verify { secondWebView.load(secondHtml, secondMeta, any()) }
    }

    private fun createWebView(): IamWebView {
        val webView = runOnMain {
            IamWebView(
                mockConcurrentHandlerHolder,
                mockJsBridgeFactory,
                mockJSCommandFactory,
                mockCurrentActivityProvider.get()
            )
        }
        return webView
    }

    private fun displayDialog(fragmentScenario: FragmentScenario<IamDialog>) {
        fragmentScenario.onFragment { dialog ->
            dialog.activity?.supportFragmentManager?.executePendingTransactions()
        }
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)
    }
}