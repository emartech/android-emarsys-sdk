package com.emarsys.mobileengage.iam.dialog

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.util.log.entry.InAppLoadingTime
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction
import com.emarsys.mobileengage.iam.webview.IamStaticWebViewProvider
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import com.emarsys.testUtil.fake.FakeActivity
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*
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
    private lateinit var uiHandler: Handler

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Rule
    @JvmField
    var activityScenarioRule: ActivityScenarioRule<FakeActivity> = ActivityScenarioRule(FakeActivity::class.java)

    @Before
    fun setUp() {
        uiHandler = Handler(Looper.getMainLooper())
        mockTimestampProvider = mock()
        val mockUuidProvider: UUIDProvider = mock {
            on { provideId() } doReturn "uuid"
        }
        setupMobileEngageComponent(FakeMobileEngageDependencyContainer(
                timestampProvider = mockTimestampProvider,
                uuidProvider = mockUuidProvider))
        initWebViewProvider()
    }

    @After
    fun tearDown() {
        tearDownMobileEngageComponent()
        setWebViewInProvider(null)
    }

    @Test
    fun testCreate_shouldReturnImageDialogInstance() {
        val fragmentScenario = launchFragment { IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider) }
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
        val fragmentScenario = launchFragment(bundle) { IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider) }
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
        val fragmentScenario = launchFragment(bundle) { IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider) }
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

        val fragmentScenario = launchFragment(bundle) { IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider) }
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

        val fragmentScenario = launchFragment(bundle) { IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider) }
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
        val fragmentScenario = launchFragment(bundle) { IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider) }
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
        val fragmentScenario = launchFragment(bundle) { IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider) }
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
            IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider)
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
        initWebViewProvider()

        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        bundle.putString(IamDialog.SID, SID)
        bundle.putString(IamDialog.URL, URL)
        bundle.putString(IamDialog.REQUEST_ID, REQUEST_ID_KEY)
        bundle.putSerializable("loading_time", InAppLoadingTime(0, 0))

        val fragmentScenario = launchFragment(bundle) { IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider) }

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

        val fragmentScenario = launchFragment(bundle) { IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider) }
        var latch = CountDownLatch(1)

        fragmentScenario.onFragment {
            it.setInAppLoadingTime(InAppLoadingTime(1, 1))
            it.activity?.runOnUiThread {
                it.dialog?.show()

                it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                it.dialog?.cancel()

                it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                latch.countDown()
            }

            latch.await()
            latch = CountDownLatch(1)
            Thread {
                it.activity?.runOnUiThread {
                    it.retainInstance shouldBe false
                    latch.countDown()
                }
            }.start()
        }
        latch.await()
    }


    @Test
    fun testDialog_dismiss_turnsRetainInstanceOff() {
        val bundle = Bundle()
        bundle.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        bundle.putString(IamDialog.SID, SID)
        bundle.putString(IamDialog.URL, URL)
        bundle.putString(IamDialog.REQUEST_ID, REQUEST_ID_KEY)

        val fragmentScenario = launchFragment(bundle) { IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider) }
        var latch = CountDownLatch(1)

        fragmentScenario.onFragment {
            it.setInAppLoadingTime(InAppLoadingTime(1, 1))
            it.activity?.runOnUiThread {
                it.dialog?.show()

                it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                it.dismiss()

                it.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                latch.countDown()
            }

            latch.await()
            latch = CountDownLatch(1)
            Thread {
                it.activity?.runOnUiThread {
                    it.retainInstance shouldBe false
                    latch.countDown()
                }
            }.start()
        }
        latch.await()
    }


    @Test
    fun testOnResume_callsActions_ifProvided() {
        val args = Bundle()
        args.putString(CAMPAIGN_ID_KEY, "123456789")
        val actions: List<OnDialogShownAction> = listOf(mock(), mock(), mock())
        val fragmentScenario = launchFragment(args) {
            IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider).apply {
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
            IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider).apply {
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
            IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider)
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
            IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider)
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
                IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider)
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
            initWebViewProvider()
            uiHandler.post {
                val webView = IamStaticWebViewProvider(getTargetContext(), uiHandler).provideWebView()
                LinearLayout(getTargetContext()).addView(webView)
            }
            val fragmentScenario = launchFragment {
                IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider)
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
    fun testOnStart_shouldNotThrowCannotAddANullChildViewToAViewGroup_exception() {
        ReflectionTestUtils.setCompanionField(IamStaticWebViewProvider.Companion, "webView", null)
        val args = Bundle()
        args.putString(IamDialog.CAMPAIGN_ID, CAMPAIGN_ID)
        args.putString(IamDialog.SID, SID)
        args.putString(IamDialog.URL, URL)
        args.putSerializable("loading_time", InAppLoadingTime(0, 0))
        try {
            val fragmentScenario = launchFragment(args) {
                IamDialog(mobileEngage().uiHandler, mobileEngage().timestampProvider)
            }

            displayDialog(fragmentScenario)
        } catch (exception: IllegalArgumentException) {
            exception.message shouldBe "The fragment has been removed from the FragmentManager already."
        }
    }

    private fun setWebViewInProvider(webView: WebView?) {
        val webViewField = IamStaticWebViewProvider::class.java.getDeclaredField("webView")
        webViewField.isAccessible = true
        webViewField[null] = webView
    }

    private fun initWebViewProvider() {
        val initLatch = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post {
            try {
                setWebViewInProvider(WebView(getTargetContext()))
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            initLatch.countDown()
        }
        initLatch.await()
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