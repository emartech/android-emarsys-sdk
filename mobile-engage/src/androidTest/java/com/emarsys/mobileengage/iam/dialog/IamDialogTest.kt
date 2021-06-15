package com.emarsys.mobileengage.iam.dialog

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.util.log.entry.InAppLoadingTime
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.iam.dialog.IamDialog.Companion.create
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction
import com.emarsys.mobileengage.iam.webview.IamStaticWebViewProvider
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.ReflectionTestUtils.setStaticField
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import com.emarsys.testUtil.fake.FakeActivity
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
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

    private lateinit var dialog: TestIamDialog
    private lateinit var mockTimestampProvider: TimestampProvider

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Rule
    @JvmField
    var activityRule = ActivityTestRule(FakeActivity::class.java)

    @Before
    @Throws(InterruptedException::class)
    fun setUp() {
        mockTimestampProvider = mock()
        setupMobileEngageComponent(FakeMobileEngageDependencyContainer(timestampProvider = mockTimestampProvider))
        activityRule.activity.runOnUiThread {
            dialog = spy(TestIamDialog.create(
                    CAMPAIGN_ID,
                    CountDownLatch(1),
                    CountDownLatch(1),
                    CountDownLatch(1),
                    CountDownLatch(1)
            ))
            dialog.setActions(listOf())
            dialog.setInAppLoadingTime(InAppLoadingTime(1, 1))
        }
        initWebViewProvider()
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        tearDownMobileEngageComponent()
        setWebViewInProvider(null)
    }

    @Test
    fun testCreate_shouldReturnImageDialogInstance() {
        runBlocking { create("", SID, URL, "requestId") } shouldNotBe null
    }

    @Test
    fun testCreate_shouldInitializeDialog_withCampaignId() {
        val campaignId = "123456789"
        val dialog = runBlocking { create(campaignId, SID, URL, "requestId") }
        val result = dialog.arguments

        result shouldNotBe null
        result!!.getString(CAMPAIGN_ID_KEY) shouldBe campaignId
    }

    @Test
    fun testCreate_shouldInitializeDialog_withRequestId() {
        val requestId = "requestId"
        val campaignId = "campaignId"
        val dialog: IamDialog = runBlocking { create(campaignId, SID, URL, requestId) }

        val result = dialog.arguments

        result shouldNotBe null
        result!!.getString(REQUEST_ID_KEY) shouldBe requestId
    }

    @Test
    fun testCreate_shouldInitializeDialog_withSid() {
        val requestId = "requestId"
        val campaignId = "campaignId"
        val dialog =  runBlocking { create(campaignId, SID, URL, requestId) }
        val result = dialog.arguments

        result shouldNotBe null
        result!!.getString("sid") shouldBe "testSid"
    }

    @Test
    fun testCreate_shouldInitializeDialog_withUrl() {
        val requestId = "requestId"
        val campaignId = "campaignId"
        val dialog =  runBlocking { create(campaignId, SID, URL, requestId) }
        val result = dialog.arguments

        result shouldNotBe null
        result!!.getString("url") shouldBe "https://www.emarsys.com"
    }

    @Test
    fun testCreate_shouldInitializeDialog_withOutRequestId() {
        val campaignId = "campaignId"
        val dialog =  runBlocking { create(campaignId, SID, URL, null) }
        val result = dialog.arguments

        result shouldNotBe null
        result!!.getString(REQUEST_ID_KEY) shouldBe null
    }

    @Test
    @Throws(InterruptedException::class)
    fun testInitialization_setsDimAmountToZero() {
        displayDialog()
        val expected = 0.0f
        val actual = dialog.dialog!!.window!!.attributes.dimAmount
        org.junit.Assert.assertEquals(expected.toDouble(), actual.toDouble(), 0.0000001)


    }

    @Test
    @Throws(InterruptedException::class)
    fun testInitialization_setsDialogToFullscreen() {
        displayDialog()
        val dialogWidth = activityRule.activity.window.attributes.width.toFloat()
        val dialogHeight = activityRule.activity.window.attributes.height.toFloat()
        val windowWidth = dialog.dialog!!.window!!.attributes.width.toFloat()
        val windowHeight = dialog.dialog!!.window!!.attributes.height.toFloat()
        org.junit.Assert.assertEquals(windowWidth.toDouble(), dialogWidth.toDouble(), 0.0001)
        org.junit.Assert.assertEquals(windowHeight.toDouble(), dialogHeight.toDouble(), 0.0001)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDialog_stillVisible_afterOrientationChange() {
        val iamDialog: IamDialog = runBlocking { create(CAMPAIGN_ID, SID, URL, REQUEST_ID_KEY) }
        val activity: AppCompatActivity = activityRule.activity
        activity.runOnUiThread { iamDialog.show(activity.supportFragmentManager, "testDialog") }
        activityRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        initWebViewProvider()
        activityRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        initWebViewProvider()
        val dialogWidth = activityRule.activity.window.attributes.width.toFloat()
        val dialogHeight = activityRule.activity.window.attributes.height.toFloat()
        val windowWidth = iamDialog.dialog!!.window!!.attributes.width.toFloat()
        val windowHeight = iamDialog.dialog!!.window!!.attributes.height.toFloat()
        org.junit.Assert.assertEquals(windowWidth.toDouble(), dialogWidth.toDouble(), 0.0001)
        org.junit.Assert.assertEquals(windowHeight.toDouble(), dialogHeight.toDouble(), 0.0001)
    }

    @Test
    fun testDialog_cancel_turnsRetainInstanceOff() {
        val iamDialog: IamDialog = runBlocking { create(CAMPAIGN_ID, SID, URL, REQUEST_ID_KEY) }
        iamDialog.setInAppLoadingTime(InAppLoadingTime(1, 1))
        val activity: AppCompatActivity = activityRule.activity
        activity.runOnUiThread { iamDialog.show(activity.supportFragmentManager, "testDialog") }
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        iamDialog.dialog!!.cancel()
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        iamDialog.retainInstance shouldBe false
    }

    @Test
    fun testDialog_dismiss_turnsRetainInstanceOff() {
        val activity: AppCompatActivity = activityRule.activity
        val iamDialog: IamDialog = runBlocking { create(CAMPAIGN_ID, SID, URL, REQUEST_ID_KEY) }

        iamDialog.setInAppLoadingTime(InAppLoadingTime(1, 1))
        iamDialog.show(activity.supportFragmentManager, "testDialog")


        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        iamDialog.dismiss()

        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        iamDialog.retainInstance shouldBe false
    }

    @Test
    fun testOnResume_callsActions_ifProvided() {
        val args = Bundle()
        args.putString(CAMPAIGN_ID_KEY, "123456789")
        dialog.arguments = args
        val actions = createMockActions()
        dialog.setActions(actions)
        displayDialog()
        for (action in actions) {
            verify(action).execute("123456789", null, null)
        }
    }

    @Test
    fun testOnResume_callsActions_onlyOnce() {
        val actions = createMockActions()
        dialog.setActions(actions)
        displayDialog()
        dismissDialog()
        displayDialog()
        for (action in actions) {
            verify(action, times(1)).execute(any(), any(), any())
        }
    }

    @Test
    fun testOnScreenTime_savesDuration_betweenResumeAndPause() {
        whenever(mockTimestampProvider.provideTimestamp()).thenReturn(100L, 250L)

        displayDialog()
        pauseDialog()
        val onScreenTime = dialog.arguments!!.getLong(ON_SCREEN_TIME_KEY)

        dialog.arguments shouldNotBe null
        onScreenTime shouldBe 150
    }

    @Test
    fun testOnScreenTime_aggregatesDurations_betweenMultipleResumeAndPause() {
        whenever(mockTimestampProvider.provideTimestamp()).thenReturn(100L, 250L, 1000L, 1003L)


        displayDialog()
        pauseDialog()
        dialog.arguments shouldNotBe null
        dialog.arguments!!.getLong(ON_SCREEN_TIME_KEY) shouldBe 150
        resumeDialog()
        pauseDialog()
        dialog.arguments shouldNotBe null
        dialog.arguments!!.getLong(ON_SCREEN_TIME_KEY) shouldBe 153
    }

    @Test
    fun testOnStart_shouldNotThrowTheSpecifiedChildAlreadyHasAParent_exception() {
        var result: Exception? = null
        try {
            displayDialog()
            val activity: AppCompatActivity = activityRule.activity
            activity.runOnUiThread { dialog.onStart() }
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
            Handler(Looper.getMainLooper()).post {
                val webView = IamStaticWebViewProvider(getTargetContext()).provideWebView()
                LinearLayout(getTargetContext()).addView(webView)
            }
            displayDialog()
            val activity: AppCompatActivity = activityRule.activity
            activity.runOnUiThread { dialog.onStart() }
        } catch (exception: IllegalStateException) {
            result = exception
        }
        result shouldBe null
    }

    @Test
    fun testOnStart_shouldNotThrowCannotAddANullChildViewToAViewGroup_exception() {
        setStaticField(IamStaticWebViewProvider::class.java, "webView", null)
        var result: Exception? = null
        try {
            displayDialog()
        } catch (exception: IllegalArgumentException) {
            result = exception
        }
        result shouldBe null
        verify(dialog).dismiss()
    }

    private fun displayDialog() {
        dialog.resumeLatch = CountDownLatch(1)
        val activity: AppCompatActivity = activityRule.activity
        activity.runOnUiThread { dialog.show(activity.supportFragmentManager, "testDialog") }
        dialog.resumeLatch.await()
    }

    private fun resumeDialog() {
        dialog.resumeLatch = CountDownLatch(1)
        val activity: Activity = activityRule.activity
        activity.runOnUiThread { dialog.onResume() }
        dialog.resumeLatch.await()
    }

    private fun pauseDialog() {
        dialog.pauseLatch = CountDownLatch(1)
        val activity: Activity = activityRule.activity
        activity.runOnUiThread { dialog.onPause() }
        dialog.pauseLatch.await()
    }

    private fun cancelDialog() {
        dialog.cancelLatch = CountDownLatch(1)
        val activity: Activity = activityRule.activity
        activity.runOnUiThread { dialog.onCancel(mock()) }
        dialog.cancelLatch.await()
    }

    private fun dismissDialog() {
        dialog.stopLatch = CountDownLatch(1)
        val activity: Activity = activityRule.activity
        activity.runOnUiThread { dialog.dismiss() }
        dialog.stopLatch.await()
    }

    @Throws(Exception::class)
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

    private fun createMockActions(): List<OnDialogShownAction> {
        return listOf(
                mock(),
                mock(),
                mock()
        )
    }

    inline fun <reified T> runBlocking(crossinline function: () -> T): T {
        var result: T? = null
        val latch: CountDownLatch = CountDownLatch(1)
        activityRule.activity.runOnUiThread {
            result = function()
            latch.countDown()
        }
        latch.await()
        return result!!
    }
}