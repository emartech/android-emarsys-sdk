package com.emarsys.mobileengage.iam

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.test.rule.ActivityTestRule
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.provider.Gettable
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.iam.dialog.IamDialog
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider
import com.emarsys.mobileengage.iam.jsbridge.*
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.webview.IamStaticWebViewProvider
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.mockito.ThreadSpy
import com.emarsys.testUtil.mockito.whenever
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.matchers.numerics.shouldBeGreaterThanOrEqual
import io.kotlintest.matchers.numerics.shouldBeLessThanOrEqual
import io.kotlintest.shouldBe
import org.json.JSONException
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.isNull
import org.mockito.Mockito.mock
import org.mockito.Mockito.timeout
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.util.concurrent.CountDownLatch

class OverlayInAppPresenterTest {
    companion object {
        private const val CAMPAIGN_ID = "555666777"
        private var SID = "testSid"
        private var URL = "https://www.emarsys.com"

        init {
            mock(Fragment::class.java)
            mock(AppCompatActivity::class.java)
        }
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    var activityRule = ActivityTestRule(FakeActivity::class.java)

    private lateinit var iamStaticWebViewProvider: IamStaticWebViewProvider
    private lateinit var mockInAppInternal: InAppInternal
    private lateinit var mockIamDialogProvider: IamDialogProvider
    private lateinit var mockButtonClickedRepository: Repository<ButtonClicked, SqlSpecification>
    private lateinit var mockDisplayedIamRepository: Repository<DisplayedIam, SqlSpecification>
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockMobileEngageInternal: MobileEngageInternal
    private lateinit var mockActivityProvider: Gettable<Activity>
    private lateinit var overlayPresenter: OverlayInAppPresenter
    private lateinit var mockIamJsBridgeFactory: IamJsBridgeFactory
    private lateinit var mockJsBridge: IamJsBridge

    @Before
    fun setUp() {
        iamStaticWebViewProvider = IamStaticWebViewProvider(InstrumentationRegistry.getTargetContext())
        mockInAppInternal = mock()
        mockIamDialogProvider = mock()
        mockButtonClickedRepository = mock()
        mockDisplayedIamRepository = mock()
        mockTimestampProvider = mock()
        mockMobileEngageInternal = mock()
        mockActivityProvider = mock()
        mockJsBridge = mock()
        mockIamJsBridgeFactory = mock {
            on { createJsBridge() } doReturn mockJsBridge
        }

        val coreSdkHandler = CoreSdkHandlerProvider().provideHandler()
        val uiHandler = Handler(Looper.getMainLooper())


        overlayPresenter = OverlayInAppPresenter(coreSdkHandler,
                uiHandler,
                iamStaticWebViewProvider,
                mockInAppInternal,
                mockIamDialogProvider,
                mockButtonClickedRepository,
                mockDisplayedIamRepository,
                mockTimestampProvider,
                mockActivityProvider,
                mockIamJsBridgeFactory)
    }

    @Test
    fun testPresent_shouldShowDialog_whenAppCompatActivity_isUsed() {
        val fragmentMock = mock(Fragment::class.java)
        val activityMock = mock(AppCompatActivity::class.java)

        val iamDialog = mock(IamDialog::class.java)
        val fragmentManager = mock(FragmentManager::class.java)

        whenever(activityMock.supportFragmentManager).thenReturn(fragmentManager)
        whenever(fragmentManager.findFragmentById(anyInt())).thenReturn(fragmentMock)
        whenever(mockActivityProvider.get()).thenReturn(activityMock)
        whenever(mockIamDialogProvider.provideDialog(any(), any(), any(), any())).thenReturn(iamDialog)

        val countDownLatch = CountDownLatch(1)

        overlayPresenter.present(
                "1",
                SID,
                URL,
                "requestId",
                0L,
                "<html><body><p>Hello</p></body></html>",
                MessageLoadedListener {
                    countDownLatch.countDown()
                }
        )

        countDownLatch.await()

        verify(iamDialog).show(any<FragmentManager>(), any())
    }

    @Test
    fun testPresent_shouldNotShowDialog_whenActivity_isUsed() {
        val iamDialog = mock(IamDialog::class.java)
        val activity = mock(Activity::class.java)

        whenever(mockActivityProvider.get()).thenReturn(activity)
        whenever(mockIamDialogProvider.provideDialog(any(), any(), any(), any())).thenReturn(iamDialog)

        val countDownLatch = CountDownLatch(1)

        overlayPresenter.present(
                "1",
                SID,
                URL,
                "requestId",
                0L,
                "<html><body><p>Hello</p></body></html>",
                MessageLoadedListener {
                    countDownLatch.countDown()
                }
        )

        countDownLatch.await()

        verify(iamDialog, times(0)).show(any<FragmentManager>(), any())
    }

    @Test
    fun testPresent_shouldNotShowDialog_whenActivity_isNull() {
        val iamDialog = mock(IamDialog::class.java)

        whenever(mockActivityProvider.get()).thenReturn(null)
        whenever(mockIamDialogProvider.provideDialog(any(), any(), any(), any())).thenReturn(iamDialog)

        val countDownLatch = CountDownLatch(1)

        overlayPresenter.present(
                "1",
                SID,
                URL,
                "requestId",
                0L,
                "<html><body><p>Hello</p></body></html>",
                MessageLoadedListener {
                    countDownLatch.countDown()
                }
        )

        countDownLatch.await()

        verify(iamDialog, times(0)).show(any<FragmentManager>(), any())
    }

    @Test
    fun testPresent_shouldNotShowDialog_whenAnotherDialog_isAlreadyShown() {
        val iamDialog = mock(IamDialog::class.java)
        val activity = mock(AppCompatActivity::class.java)
        val fragmentManager = mock(FragmentManager::class.java)
        val fragment = mock(Fragment::class.java)

        whenever(mockIamDialogProvider.provideDialog(any(), any(), any(), any())).thenReturn(iamDialog)
        whenever(mockActivityProvider.get()).thenReturn(activity)
        whenever(activity.supportFragmentManager).thenReturn(fragmentManager)
        whenever(fragmentManager.findFragmentByTag("MOBILE_ENGAGE_IAM_DIALOG_TAG")).thenReturn(fragment)

        val countDownLatch = CountDownLatch(1)

        overlayPresenter.present(
                "1",
                SID,
                URL,
                "requestId",
                0L,
                "<html><body><p>Hello</p></body></html>",
                MessageLoadedListener {
                    countDownLatch.countDown()
                }
        )

        countDownLatch.await()

        verify(iamDialog, times(0)).show(any<FragmentManager>(), any())
    }

    @Test
    fun testPresent_shouldSet_JsBridgeOnCloseListener() {
        val iamJsBridge: IamJsBridge = mock()
        whenever(mockIamJsBridgeFactory.createJsBridge()).thenReturn(iamJsBridge)
        val iamDialog = mock(IamDialog::class.java)
        whenever(mockIamDialogProvider.provideDialog(any(), any(), any(), any())).thenReturn(iamDialog)

        overlayPresenter.present("1", SID, URL, "requestId", 0L, "<html><body><p>Hello</p></body></html>", null)

        verify(iamJsBridge).onCloseListener = any<OnCloseListener>()
    }

    @Test
    fun testOnClose_shouldCallDismissOnFragment() {
        val fragment: DialogFragment = mock()
        val supportManager: FragmentManager = mock()
        val activity: AppCompatActivity = mock()
        whenever(activity.supportFragmentManager).thenReturn(supportManager)
        whenever(supportManager.findFragmentByTag(any())).thenReturn(fragment)
        whenever(mockActivityProvider.get()).thenReturn(activity)

        overlayPresenter.onCloseTriggered().invoke()

        verify(fragment).dismiss()
    }

    @Test
    fun testPresent_shouldSet_JsBridgeOnButtonClickedListener() {
        val iamJsBridge: IamJsBridge = mock()
        whenever(mockIamJsBridgeFactory.createJsBridge()).thenReturn(iamJsBridge)
        val iamDialog = mock(IamDialog::class.java)
        whenever(mockIamDialogProvider.provideDialog(any(), any(), any(), any())).thenReturn(iamDialog)

        overlayPresenter.present("1", SID, URL, "requestId", 0L, "<html><body><p>Hello</p></body></html>", null)

        verify(iamJsBridge).onButtonClickedListener = any<OnButtonClickedListener>()
    }

    @Test
    fun testButtonClicked_shouldStoreButtonClick_inRepository() {
        val buttonClickedArgumentCaptor = ArgumentCaptor.forClass(ButtonClicked::class.java)
        val id = "12346789"
        val buttonId = "987654321"
        val json = JSONObject().put("id", id).put("buttonId", buttonId)
        val before = System.currentTimeMillis()

        overlayPresenter.onButtonClickedTriggered(CAMPAIGN_ID, SID, URL).invoke(buttonId, json)

        verify(mockButtonClickedRepository, timeout(1000)).add(capture<ButtonClicked>(buttonClickedArgumentCaptor))
        val buttonClicked = buttonClickedArgumentCaptor.value
        val after = System.currentTimeMillis()
        buttonClicked.campaignId shouldBe CAMPAIGN_ID
        buttonClicked.buttonId shouldBe buttonId
        buttonClicked.timestamp shouldBeGreaterThanOrEqual before
        buttonClicked.timestamp shouldBeLessThanOrEqual after
    }

    @Test
    fun testButtonClicked_shouldSendInternalEvent_throughInAppInternal_withSidAndUrl() {
        val id = "12346789"
        val buttonId = "987654321"
        val json = JSONObject().put("id", id).put("buttonId", buttonId)
        val attributes: MutableMap<String, String> = HashMap()
        attributes["campaignId"] = CAMPAIGN_ID
        attributes["buttonId"] = buttonId
        attributes["sid"] = SID
        attributes["url"] = URL

        overlayPresenter.onButtonClickedTriggered(CAMPAIGN_ID, SID, URL).invoke(buttonId, json)
        verify(mockInAppInternal, timeout(1000)).trackInternalCustomEvent("inapp:click", attributes, null)
    }

    @Test
    fun testButtonClicked_shouldSendInternalEvent_throughInAppInternal_whenSidAndUrlIsNull() {
        val id = "12346789"
        val buttonId = "987654321"
        val json = JSONObject().put("id", id).put("buttonId", buttonId)
        val attributes: MutableMap<String, String> = HashMap()
        attributes["campaignId"] = CAMPAIGN_ID
        attributes["buttonId"] = buttonId

        overlayPresenter.onButtonClickedTriggered(CAMPAIGN_ID, null, null).invoke(buttonId, json)
        verify(mockInAppInternal, timeout(1000)).trackInternalCustomEvent("inapp:click", attributes, null)
    }

    @Test
    fun testPresent_shouldSet_JsBridgeOnMEEventListener() {
        val iamJsBridge: IamJsBridge = mock()
        whenever(mockIamJsBridgeFactory.createJsBridge()).thenReturn(iamJsBridge)
        val iamDialog = mock(IamDialog::class.java)
        whenever(mockIamDialogProvider.provideDialog(any(), any(), any(), any())).thenReturn(iamDialog)

        overlayPresenter.present("1", SID, URL, "requestId", 0L, "<html><body><p>Hello</p></body></html>", null)

        verify(iamJsBridge).onMEEventListener = any<OnMEEventListener>()
    }

    @Test
    fun testPresent_shouldSet_JsBridgeExternalUrlEventListener() {
        val iamJsBridge: IamJsBridge = mock()
        whenever(mockIamJsBridgeFactory.createJsBridge()).thenReturn(iamJsBridge)
        val iamDialog = mock(IamDialog::class.java)
        whenever(mockIamDialogProvider.provideDialog(any(), any(), any(), any())).thenReturn(iamDialog)

        overlayPresenter.present("1", SID, URL, "requestId", 0L, "<html><body><p>Hello</p></body></html>", null)

        verify(iamJsBridge).onOpenExternalUrlListener = any<OnOpenExternalUrlListener>()
    }

    @Test
    @Throws(Exception::class)
    fun testOpenExternalLink_shouldStartActivity_withViewIntent() {
        val activity = mock<Activity>()
        whenever(activity.packageManager).thenReturn(activityRule.activity.packageManager)
        whenever(mockActivityProvider.get()).thenReturn(activity)
        val id = "12346789"
        val url = "https://emarsys.com"
        val json = JSONObject().put("id", id).put("url", url)

        overlayPresenter.onExternalUrlTriggered().invoke(url, json)
        val captor = ArgumentCaptor.forClass(Intent::class.java)
        verify(activity, timeout(1000)).startActivity(capture<Intent>(captor))
        val intent = captor.value
        Intent.ACTION_VIEW shouldBe intent.action
        Uri.parse(url) shouldBe intent.data
    }

    @Test
    fun testPresent_OnInAppEventListenerTriggered_shouldCallHandleApplicationEventMethodOnInAppMessageHandler() {
        val activity: Activity = mock()
        val mockEventHandler: EventHandler = mock()
        whenever(mockActivityProvider.get()).thenReturn(activity)
        whenever(mockInAppInternal.eventHandler).thenReturn(mockEventHandler)

        val payload = JSONObject()
                .put("payloadKey1",
                        JSONObject()
                                .put("payloadKey2", "payloadValue1"))
        val json = JSONObject()
                .put("name", "eventName")
                .put("id", "123456789")
                .put("payload", payload)

        overlayPresenter.onAppEventTriggered().invoke("eventName", json)

        verify(mockEventHandler, timeout(1000)).handleEvent(activity, "eventName", payload)
    }

    @Test
    fun testTriggerAppEvent_inAppMessageHandler_calledOnMainThread() {
        val activity: Activity = mock()
        val mockEventHandler: EventHandler = mock()
        whenever(mockActivityProvider.get()).thenReturn(activity)
        whenever(mockInAppInternal.eventHandler).thenReturn(mockEventHandler)

        val threadSpy: ThreadSpy<*> = ThreadSpy<Any?>()
        whenever(mockInAppInternal.eventHandler.handleEvent(anyOrNull(), anyOrNull(), anyOrNull())).doAnswer(threadSpy)

        val id = "12346789"
        val eventName = "eventName"
        val json = JSONObject()
                .put("id", id)
                .put("name", eventName)

        overlayPresenter.onAppEventTriggered().invoke("eventName", json)

        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    fun testTriggerMeEvent_shouldCallMobileEngageInternal_withAttributes() {
        val eventAttributes: MutableMap<String, String> = HashMap()
        eventAttributes["payloadKey1"] = "value1"
        eventAttributes["payloadKey2"] = "value2"
        val json = JSONObject()
                .put("name", "eventName")
                .put("id", "123456789")
                .put("payload",
                        JSONObject()
                                .put("payloadKey1", "value1")
                                .put("payloadKey2", "value2"))

        overlayPresenter.onMEEventTriggered().invoke("eventName", json)

        verify(mockInAppInternal, timeout(1000)).trackCustomEventAsync("eventName", eventAttributes, null)
    }

    @Test
    @Throws(JSONException::class)
    fun testTriggerMeEvent_shouldCallMobileEngageInternal_withoutAttributes() {
        val json = JSONObject()
                .put("name", "eventName")
                .put("id", "123456789")

        overlayPresenter.onMEEventTriggered().invoke("eventName", json)

        verify(mockInAppInternal, timeout(1000)).trackCustomEventAsync("eventName", null, null)
    }

    @Test
    fun testTriggerMeEvent_shouldCallMobileEngageInternal_onCoreSDKThread() {
        val threadSpy: ThreadSpy<*> = ThreadSpy<Any?>()
        whenever(mockInAppInternal.trackCustomEventAsync(any(), anyOrNull(), isNull())).doAnswer(threadSpy)
        val id = "12346789"
        val eventName = "eventName"
        val json = JSONObject()
                .put("id", id)
                .put("name", eventName)

        overlayPresenter.onMEEventTriggered().invoke("eventName", json)

        threadSpy.verifyCalledOnCoreSdkThread()
    }
}