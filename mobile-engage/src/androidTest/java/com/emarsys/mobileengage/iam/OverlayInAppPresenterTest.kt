package com.emarsys.mobileengage.iam

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
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
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
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
    var appcompatActivityRule = ActivityTestRule(FakeActivity::class.java)

    private lateinit var mockInAppInternal: InAppInternal
    private lateinit var mockIamDialogProvider: IamDialogProvider
    private lateinit var mockButtonClickedRepository: Repository<ButtonClicked, SqlSpecification>
    private lateinit var mockDisplayedIamRepository: Repository<DisplayedIam, SqlSpecification>
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockMobileEngageInternal: MobileEngageInternal
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider
    private lateinit var overlayPresenter: OverlayInAppPresenter
    private lateinit var mockClipboardManager: ClipboardManager
    private lateinit var mockIamJsBridgeFactory: IamJsBridgeFactory
    private lateinit var mockJsBridge: IamJsBridge
    private lateinit var mockJSCommandFactory: JSCommandFactory
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var iamDialog: IamDialog
    private lateinit var context: Context
    private lateinit var latch: CountDownLatch
    private lateinit var spyOverlayPresenter: OverlayInAppPresenter

    var html = String.format(
        """<!DOCTYPE html>
<html lang="en">
  <head>
    <script>
      window.onload = function() {
      };
        Android.%s("{success:true}");
    </script>
  </head>
  <body style="background: transparent;">
  </body>
</html>""", "onPageLoaded"
    )

    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        latch = CountDownLatch(1)
        iamDialog = mock()
        mockInAppInternal = mock()
        mockIamDialogProvider = mock {
            on { provideDialog(anyNotNull(), any(), any(), any()) }
                .thenReturn(iamDialog)
        }
        mockButtonClickedRepository = mock()
        mockDisplayedIamRepository = mock()
        mockTimestampProvider = mock()
        mockMobileEngageInternal = mock()
        mockCurrentActivityProvider = mock()
        mockJsBridge = mock()
        mockClipboardManager = mock()
        mockJSCommandFactory = mock()
        mockIamJsBridgeFactory = mock {
            on { createJsBridge(anyOrNull(), anyOrNull()) } doReturn mockJsBridge
        }
        overlayPresenter = OverlayInAppPresenter(
            concurrentHandlerHolder,
            mockInAppInternal,
            mockIamDialogProvider,
            mockButtonClickedRepository,
            mockDisplayedIamRepository,
            mockTimestampProvider,
            mockCurrentActivityProvider,
            mockIamJsBridgeFactory,
            mockClipboardManager
        )
        spyOverlayPresenter = spyOverlayPresenter()
    }

    @Test
    fun testPresent_shouldShowDialog_whenFragmentActivity_isUsed() {
        whenever(mockCurrentActivityProvider.get()).thenReturn(appcompatActivityRule.activity as FragmentActivity)

        val countDownLatch = CountDownLatch(1)

        spyOverlayPresenter.present(
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
        whenever(mockCurrentActivityProvider.get()).thenReturn(appcompatActivityRule.activity)

        val countDownLatch = CountDownLatch(1)

        spyOverlayPresenter.present(
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
        val activity: Activity = mock()

        whenever(mockCurrentActivityProvider.get()).thenReturn(activity)
        whenever(mockIamDialogProvider.provideDialog(anyNotNull(), any(), any(), any())).thenReturn(
            iamDialog
        )

        val countDownLatch = CountDownLatch(1)

        spyOverlayPresenter.present(
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
        whenever(mockCurrentActivityProvider.get()).thenReturn(null)
        whenever(mockIamDialogProvider.provideDialog(anyNotNull(), any(), any(), any())).thenReturn(
            iamDialog
        )

        val countDownLatch = CountDownLatch(1)

        spyOverlayPresenter.present(
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
        val activity: AppCompatActivity = mock()
        val fragmentManager: FragmentManager = mock()
        val fragment: Fragment = mock()

        whenever(mockIamDialogProvider.provideDialog(anyNotNull(), any(), any(), any())).thenReturn(
            iamDialog
        )
        whenever(mockCurrentActivityProvider.get()).thenReturn(activity)
        whenever(activity.supportFragmentManager).thenReturn(fragmentManager)
        whenever(fragmentManager.findFragmentByTag("MOBILE_ENGAGE_IAM_DIALOG_TAG")).thenReturn(
            fragment
        )

        val countDownLatch = CountDownLatch(1)

        spyOverlayPresenter.present(
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
        whenever(mockCurrentActivityProvider.get()).thenReturn(activity)

        overlayPresenter.onCloseTriggered().invoke()
        val latch = CountDownLatch(1)
        concurrentHandlerHolder.postOnMain { latch.countDown() }
        latch.await()

        verify(fragment).dismiss()
    }

    @Test
    fun testPresent_OnInAppEventListenerTriggered_shouldCallHandleApplicationEventMethodOnInAppMessageHandler() {
        val activity: Activity = mock()
        val mockEventHandler: EventHandler = mock()
        whenever(mockCurrentActivityProvider.get()).thenReturn(activity)
        whenever(mockInAppInternal.eventHandler).thenReturn(mockEventHandler)

        val payload = JSONObject()
            .put(
                "payloadKey1",
                JSONObject()
                    .put("payloadKey2", "payloadValue1")
            )
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
        whenever(mockCurrentActivityProvider.get()).thenReturn(activity)
        whenever(mockInAppInternal.eventHandler).thenReturn(mockEventHandler)

        val threadSpy: ThreadSpy<*> = ThreadSpy<Any?>()
        whenever(
            mockInAppInternal.eventHandler?.handleEvent(
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).doAnswer(threadSpy)

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
        whenever(mockCurrentActivityProvider.get()).thenReturn(null)
        whenever(mockInAppInternal.eventHandler).thenReturn(mockEventHandler)

        val id = "12346789"
        val eventName = "eventName"
        val json = JSONObject()
            .put("id", id)
            .put("name", eventName)

        overlayPresenter.onAppEventTriggered().invoke("eventName", json)

        verifyNoInteractions(mockEventHandler)
    }

    private fun spyOverlayPresenter(): OverlayInAppPresenter {
        val spyOverlayPresenter = spy(overlayPresenter)

        doAnswer {
            (it.getArgument(3) as MessageLoadedListener).onMessageLoaded()
        }.whenever(spyOverlayPresenter).loadMessageAsync(
            any(),
            any(),
            any(),
            any()
        )
        return spyOverlayPresenter
    }
}