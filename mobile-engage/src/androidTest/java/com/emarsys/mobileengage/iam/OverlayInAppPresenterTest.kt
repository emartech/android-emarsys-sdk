package com.emarsys.mobileengage.iam

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.test.rule.ActivityTestRule
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.iam.dialog.IamDialog
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactory
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.webview.IamStaticWebViewProvider
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.mockito.ThreadSpy
import com.emarsys.testUtil.mockito.anyNotNull
import com.emarsys.testUtil.mockito.whenever
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*
import java.util.concurrent.CountDownLatch

class OverlayInAppPresenterTest {
    companion object {
        private var SID = "testSid"
        private var URL = "https://www.emarsys.com"
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
    private lateinit var mockActivityProvider: CurrentActivityProvider
    private lateinit var overlayPresenter: OverlayInAppPresenter
    private lateinit var mockIamJsBridgeFactory: IamJsBridgeFactory
    private lateinit var mockJsBridge: IamJsBridge
    private lateinit var mockJSCommandFactory: JSCommandFactory
    private lateinit var uiHandler: Handler

    @Before
    fun setUp() {
        iamStaticWebViewProvider = IamStaticWebViewProvider(InstrumentationRegistry.getTargetContext(), Handler(Looper.getMainLooper()))
        mockInAppInternal = mock()
        mockIamDialogProvider = mock()
        mockButtonClickedRepository = mock()
        mockDisplayedIamRepository = mock()
        mockTimestampProvider = mock()
        mockMobileEngageInternal = mock()
        mockActivityProvider = mock()
        mockJsBridge = mock()
        mockJSCommandFactory = mock()
        mockIamJsBridgeFactory = mock {
            on { createJsBridge(anyOrNull(), anyOrNull()) } doReturn mockJsBridge
        }

        val coreSdkHandler = CoreSdkHandlerProvider().provideHandler()
        uiHandler = Handler(Looper.getMainLooper())


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
    fun testPresent_shouldShowDialog_whenFragmentActivity_isUsed() {
        val fragmentMock: Fragment = mock()
        val activityMock: FragmentActivity = mock()

        val iamDialog: IamDialog = mock()
        val fragmentManager: FragmentManager = mock()

        whenever(activityMock.supportFragmentManager).thenReturn(fragmentManager)
        whenever(fragmentManager.findFragmentById(any())).thenReturn(fragmentMock)
        whenever(mockActivityProvider.get()).thenReturn(activityMock)
        whenever(mockIamDialogProvider.provideDialog(anyNotNull(), any(), any(), any())).thenReturn(iamDialog)

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
    fun testPresent_shouldShowDialog_whenAppCompatActivity_isUsed() {
        val fragmentMock: Fragment = mock()
        val activityMock: AppCompatActivity = mock()

        val iamDialog: IamDialog = mock()
        val fragmentManager: FragmentManager = mock()

        whenever(activityMock.supportFragmentManager).thenReturn(fragmentManager)
        whenever(fragmentManager.findFragmentById(any())).thenReturn(fragmentMock)
        whenever(mockActivityProvider.get()).thenReturn(activityMock)
        whenever(mockIamDialogProvider.provideDialog(anyNotNull(), any(), any(), any())).thenReturn(iamDialog)

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
        val iamDialog: IamDialog = mock()
        val activity: Activity = mock()

        whenever(mockActivityProvider.get()).thenReturn(activity)
        whenever(mockIamDialogProvider.provideDialog(anyNotNull(), any(), any(), any())).thenReturn(iamDialog)

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
        val iamDialog: IamDialog = mock()

        whenever(mockActivityProvider.get()).thenReturn(null)
        whenever(mockIamDialogProvider.provideDialog(anyNotNull(), any(), any(), any())).thenReturn(iamDialog)

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
        val iamDialog: IamDialog = mock()
        val activity: AppCompatActivity = mock()
        val fragmentManager: FragmentManager = mock()
        val fragment: Fragment = mock()

        whenever(mockIamDialogProvider.provideDialog(anyNotNull(), any(), any(), any())).thenReturn(iamDialog)
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
    fun testOnClose_shouldCallDismissOnFragment() {
        val fragment: DialogFragment = mock()
        val supportManager: FragmentManager = mock()
        val activity: AppCompatActivity = mock()
        whenever(activity.supportFragmentManager).thenReturn(supportManager)
        whenever(supportManager.findFragmentByTag(any())).thenReturn(fragment)
        whenever(mockActivityProvider.get()).thenReturn(activity)

        overlayPresenter.onCloseTriggered().invoke()
        val latch = CountDownLatch(1)
        uiHandler.post { latch.countDown() }
        latch.await()

        verify(fragment).dismiss()
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
        whenever(mockInAppInternal.eventHandler?.handleEvent(anyOrNull(), anyOrNull(), anyOrNull())).doAnswer(threadSpy)

        val id = "12346789"
        val eventName = "eventName"
        val json = JSONObject()
                .put("id", id)
                .put("name", eventName)

        overlayPresenter.onAppEventTriggered().invoke("eventName", json)

        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    fun testTriggerAppEvent_inAppMessageHandler_shouldNotBeCalledWhenActivityIsNull() {
        val mockEventHandler: EventHandler = mock()
        whenever(mockActivityProvider.get()).thenReturn(null)
        whenever(mockInAppInternal.eventHandler).thenReturn(mockEventHandler)

        val id = "12346789"
        val eventName = "eventName"
        val json = JSONObject()
                .put("id", id)
                .put("name", eventName)

        overlayPresenter.onAppEventTriggered().invoke("eventName", json)

        verifyNoInteractions(mockEventHandler)
    }
}