package com.emarsys.mobileengage.iam.dialog

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.webkit.WebView
import android.widget.LinearLayout
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
import com.emarsys.testUtil.fake.FakeActivity
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch

class IamDialogTest {
    private companion object {
        const val CAMPAIGN_ID = "id_value"
        private const val SID = "testSid"
        private const val URL = "https://www.emarsys.com"
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
    private lateinit var scenario: ActivityScenario<FakeActivity>

    @BeforeEach
    fun setUp() {
        scenario = ActivityScenario.launch(FakeActivity::class.java)
        val countDownLatch = CountDownLatch(1)
        scenario.onActivity { activity ->
            mockTimestampProvider = mock()
            val mockUuidProvider: UUIDProvider = mock {
                on { provideId() } doReturn "uuid"
            }

            mockJsBridge = mock()
            mockJsBridgeFactory = mock {
                on { createJsBridge(any()) } doReturn mockJsBridge
            }

            mockJSCommandFactory = mock()
            mockJSCommandFactoryProvider = mock {
                on { provide() } doReturn mockJSCommandFactory
            }

            mockConcurrentHandlerHolder = mock()
            mockCurrentActivityProvider = mock {
                on { get() } doReturn activity
            }

            val iamWebView = createWebView()
            mockWebViewFactory = mock {
                on { create(null) } doReturn iamWebView
            }

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
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @AfterEach
    fun tearDown() {
        tearDownMobileEngageComponent()
        scenario.close()
    }

    @Test
    fun testConstructor_forTimestampProvider() {
        val dialogTimestampProvider =
            ReflectionTestUtils.getInstanceField<TimestampProvider>(iamDialog, "timestampProvider")

        dialogTimestampProvider shouldBe mockTimestampProvider
    }

    @Test
    fun testCreate_shouldReturnIamDialogInstance() {
        val fragmentScenario =
            launchFragment {
                IamDialog(
                    mobileEngage().timestampProvider,
                    mobileEngage().webViewFactory
                )
            }
        fragmentScenario.onFragment { fragment ->
            fragment shouldNotBe null
        }
    }

    @Test
    fun testCreate_shouldInitializeDialog_withCampaignId() {
        val campaignId = "123456789"
        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, campaignId)
        bundle.putString(IamDialog.SID, null)
        bundle.putString(IamDialog.URL, null)
        bundle.putString(IamDialog.REQUEST_ID, null)
        val fragmentScenario = launchFragment(bundle) {
            IamDialog(
                mobileEngage().timestampProvider,
                mobileEngage().webViewFactory
            )
        }
        fragmentScenario.onFragment { fragment ->
            val result = fragment.arguments

            result shouldNotBe null
            result!!.getString(CAMPAIGN_ID_KEY) shouldBe campaignId
        }
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
        val fragmentScenario = launchFragment(bundle) {
            IamDialog(
                mobileEngage().timestampProvider,
                mobileEngage().webViewFactory
            )
        }
        fragmentScenario.onFragment { fragment ->
            val result = fragment.arguments

            result shouldNotBe null
            result!!.getString(REQUEST_ID_KEY) shouldBe requestId
        }
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

        val fragmentScenario = launchFragment(bundle) {
            IamDialog(
                mobileEngage().timestampProvider,
                mobileEngage().webViewFactory
            )
        }
        fragmentScenario.onFragment { fragment ->
            val result = fragment.arguments

            result shouldNotBe null
            result!!.getString("sid") shouldBe "testSid"
        }
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

        val fragmentScenario = launchFragment(bundle) {
            IamDialog(
                mobileEngage().timestampProvider,
                mobileEngage().webViewFactory
            )
        }
        fragmentScenario.onFragment { fragment ->
            val result = fragment.arguments

            result shouldNotBe null
            result!!.getString("url") shouldBe "https://www.emarsys.com"
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
        val fragmentScenario = launchFragment(bundle) {
            IamDialog(
                mobileEngage().timestampProvider,
                mobileEngage().webViewFactory
            )
        }
        fragmentScenario.onFragment { fragment ->
            val result = fragment.arguments

            result shouldNotBe null
            result!!.getString(REQUEST_ID_KEY) shouldBe null
        }
    }

    @Test
    fun testInitialization_setsDimAmountToZero() {

        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        bundle.putString(IamDialog.SID, SID)
        bundle.putString(IamDialog.URL, URL)
        bundle.putString(IamDialog.REQUEST_ID, null)
        val fragmentScenario = launchFragment(bundle) {
            IamDialog(
                mobileEngage().timestampProvider,
                mobileEngage().webViewFactory
            )
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
        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        bundle.putString(IamDialog.SID, SID)
        bundle.putString(IamDialog.URL, URL)
        bundle.putString(IamDialog.REQUEST_ID, null)

        val fragmentScenario = launchFragment(bundle) {
            IamDialog(
                mobileEngage().timestampProvider,
                mobileEngage().webViewFactory
            )
        }

        displayDialog(fragmentScenario)
        fragmentScenario.onFragment {
            val windowWidth = it.activity!!.window.attributes.width
            val windowHeight = it.activity!!.window.attributes.height

            val dialogWidth = it.dialog!!.window!!.attributes.width
            val dialogHeight = it.dialog!!.window!!.attributes.height

            dialogWidth shouldBe windowWidth
            dialogHeight shouldBe windowHeight
        }
    }

    @Test
    fun testDialog_stillVisible_afterOrientationChange() {
        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        bundle.putString(IamDialog.SID, SID)
        bundle.putString(IamDialog.URL, URL)
        bundle.putString(IamDialog.REQUEST_ID, REQUEST_ID_KEY)
        bundle.putSerializable("loading_time", InAppLoadingTime(0, 0))

        val fragmentScenario = launchFragment(bundle) {
            IamDialog(
                mobileEngage().timestampProvider,
                mobileEngage().webViewFactory
            )
        }

        displayDialog(fragmentScenario)

        fragmentScenario.onFragment {
            it.activity?.runOnUiThread {
                it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            it.activity?.runOnUiThread {
                it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            val dialogWidth = it.activity?.window?.attributes?.width ?: 0
            val dialogHeight = it.activity?.window?.attributes?.height ?: 0
            val windowWidth = it.dialog!!.window!!.attributes.width
            val windowHeight = it.dialog!!.window!!.attributes.height

            dialogWidth shouldBe windowWidth
            dialogHeight shouldBe windowHeight
        }
    }

    @Test
    fun testDialog_cancel_turnsRetainInstanceOff() {
        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        bundle.putString(IamDialog.SID, SID)
        bundle.putString(IamDialog.URL, URL)
        bundle.putString(IamDialog.REQUEST_ID, REQUEST_ID_KEY)

        val fragmentScenario =
            launchFragment(bundle) {
                iamDialog
            }
        val fragmentLatch = CountDownLatch(1)

        displayDialog(fragmentScenario)
        fragmentScenario.onFragment {
            it.setInAppLoadingTime(InAppLoadingTime(1, 1))
            it.activity?.runOnUiThread {
                it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                it.onCancel(it.dialog!!)

                it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                it.retainInstance shouldBe false
            }
            fragmentLatch.countDown()
        }
        fragmentLatch.await()
    }


    @Test
    fun testDialog_dismiss_turnsRetainInstanceOff() {
        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        bundle.putString(IamDialog.SID, SID)
        bundle.putString(IamDialog.URL, URL)
        bundle.putString(IamDialog.REQUEST_ID, REQUEST_ID_KEY)

        val fragmentScenario = launchFragment(bundle) {
            iamDialog
        }
        val fragmentLatch = CountDownLatch(1)

        displayDialog(fragmentScenario)
        fragmentScenario.onFragment {
            it.setInAppLoadingTime(InAppLoadingTime(1, 1))
            it.activity?.runOnUiThread {
                it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                it.dismiss()

                it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                it.retainInstance shouldBe false
                fragmentLatch.countDown()
            }
        }
        fragmentLatch.await()
    }

    @Test
    fun testOnResume_callsActions_ifProvided() {
        val args = Bundle()
        args.putString(CAMPAIGN_ID_KEY, "123456789")
        val actions: List<OnDialogShownAction> = listOf(mock(), mock(), mock())
        val fragmentScenario = launchFragment(args) {
            iamDialog.apply {
                setActions(actions)
            }
        }
        fragmentScenario.onFragment {
            displayDialog(fragmentScenario)

            for (action in actions) {
                verify(action).execute("123456789", null, null)
            }
        }
    }

    @Test
    fun testOnResume_callsActions_onlyOnce() {
        val actions: List<OnDialogShownAction> = listOf(mock(), mock(), mock())

        val args = Bundle()
        args.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        args.putString(IamDialog.SID, SID)
        args.putString(IamDialog.URL, URL)
        val fragmentScenario = launchFragment(args) {
            iamDialog.apply {
                setActions(actions)
            }
        }
        fragmentScenario.moveToState(Lifecycle.State.CREATED)
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)
        fragmentScenario.moveToState(Lifecycle.State.CREATED)
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)
        fragmentScenario.onFragment {
            for (action in actions) {
                verify(action, times(1)).execute(any(), any(), any())
            }
        }
    }

    @Test
    fun testOnScreenTime_savesDuration_betweenResumeAndPause() {
        whenever(mockTimestampProvider.provideTimestamp()).thenReturn(100L, 250L)
        val args = Bundle()
        args.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        args.putString(IamDialog.SID, SID)
        args.putString(IamDialog.URL, URL)
        val fragmentScenario = launchFragment(args) {
            iamDialog
        }

        fragmentScenario.onFragment {
            it.activity?.runOnUiThread {
                it.onPause()
            }
            val onScreenTime = it.arguments?.getLong(ON_SCREEN_TIME_KEY) ?: -1

            it.arguments shouldNotBe null
            onScreenTime shouldBe 150

        }
    }

    @Test
    fun testOnScreenTime_aggregatesDurations_betweenMultipleResumeAndPause() {
        whenever(mockTimestampProvider.provideTimestamp()).thenReturn(100L, 250L, 1000L, 1003L)

        val args = Bundle()
        args.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        args.putString(IamDialog.SID, SID)
        args.putString(IamDialog.URL, URL)
        val fragmentScenario = launchFragment(args) {
            iamDialog
        }
        fragmentScenario.onFragment {
            it.activity?.runOnUiThread {
                it.onPause()
            }
            it.arguments shouldNotBe null
            it.arguments!!.getLong(ON_SCREEN_TIME_KEY) shouldBe 150

            it.activity?.runOnUiThread {
                it.onResume()
                it.onPause()
            }
            it.arguments shouldNotBe null
            it.arguments!!.getLong(ON_SCREEN_TIME_KEY) shouldBe 153
        }
    }

    @Test
    fun testOnStart_shouldNotThrowTheSpecifiedChildAlreadyHasAParent_exception() {
        var result: Exception? = null
        try {
            val fragmentScenario = launchFragment {
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
        var result: Exception? = null
        try {
            val webView = runOnMain {
                val webView = WebView(getTargetContext())
                LinearLayout(getTargetContext()).addView(webView)
                webView
            }
            val iamWebView = createWebView()
            iamWebView.webView = webView

            whenever(mockWebViewFactory.create(null)).thenReturn(iamWebView)

            val fragmentScenario = launchFragment {
                IamDialog(
                    mobileEngage().timestampProvider,
                    mobileEngage().webViewFactory
                )
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
    fun testLoadInApp() {
        val html = "<html></html>"
        val inAppMetaData = InAppMetaData(CAMPAIGN_ID, null, null)
        val messageLoadedListener = MessageLoadedListener { }

        val mockIamWebView: IamWebView = mock()
        whenever(mockWebViewFactory.create(null)).thenReturn(mockIamWebView)

        val dialog = IamDialog(
            mobileEngage().timestampProvider,
            mockWebViewFactory
        )

        ReflectionTestUtils.setInstanceField(dialog, "iamWebView", mockIamWebView)

        dialog.loadInApp(html, inAppMetaData, messageLoadedListener)

        verify(mockIamWebView).load(html, inAppMetaData, messageLoadedListener)
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
            dialog.activity?.runOnUiThread {
                dialog.activity?.supportFragmentManager?.executePendingTransactions()
            }
        }
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)
    }
}