package com.emarsys.mobileengage.iam

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.test.core.app.ActivityScenario
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.iam.dialog.IamDialog
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.mockito.anyNotNull
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.concurrent.CountDownLatch

class OverlayInAppPresenterTest : AnnotationSpec() {
    companion object {
        private var SID = "testSid"
        private var URL = "https://www.emarsys.com"
    }

    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockIamDialogProvider: IamDialogProvider
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider
    private lateinit var mockIamDialog: IamDialog

    private lateinit var inAppPresenter: OverlayInAppPresenter

    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockIamDialog = mock()
        mockIamDialogProvider = mock {
            on { provideDialog(any(), any(), any(), any()) } doReturn mockIamDialog
        }
        mockTimestampProvider = mock()
        mockCurrentActivityProvider = mock()

        whenever(mockIamDialog.loadInApp(any(), any(), any())).thenAnswer {
            (it.getArgument(2) as MessageLoadedListener).onMessageLoaded()
        }

        inAppPresenter = OverlayInAppPresenter(
            concurrentHandlerHolder,
            mockIamDialogProvider,
            mockTimestampProvider,
            mockCurrentActivityProvider
        )
    }

    @Test
    fun testPresent_shouldCallCallback_whenDialogIsShown() {
        val scenario = ActivityScenario.launch(FakeActivity::class.java)
        val countDownLatch = CountDownLatch(1)
        var callbackCalled = false
        Thread {
            scenario.onActivity { activity ->
                whenever(mockCurrentActivityProvider.get()).thenReturn(activity)

                inAppPresenter.present(
                    "1",
                    SID,
                    URL,
                    "requestId",
                    0L,
                    "<html><body><p>Hello</p></body></html>"
                ) {
                    callbackCalled = true
                    countDownLatch.countDown()
                }
            }

        }.start()
        countDownLatch.await()
        scenario.close()
        callbackCalled shouldBe true
    }

    @Test
    fun testPresent_shouldShowDialog_whenAppCompatActivity_isUsed() {
        val scenario = ActivityScenario.launch(FakeActivity::class.java)
        val countDownLatch = CountDownLatch(1)
        var callbackCalled = false
        Thread {
            scenario.onActivity { activity ->
                whenever(mockCurrentActivityProvider.get()).thenReturn(activity)

                inAppPresenter.present(
                    "1",
                    SID,
                    URL,
                    "requestId",
                    0L,
                    "<html><body><p>Hello</p></body></html>"
                ) {
                    callbackCalled = true
                    countDownLatch.countDown()
                }
            }
        }.start()
        countDownLatch.await()
        scenario.close()
        verify(mockIamDialog).show(any<FragmentManager>(), any())
        callbackCalled shouldBe true
    }

    @Test
    fun testPresent_shouldNotShowDialog_whenActivity_isUsed() {
        val activity: Activity = mock()
        val countDownLatch = CountDownLatch(1)
        var callbackCalled = false
        Thread {
            whenever(mockCurrentActivityProvider.get()).thenReturn(activity)

            inAppPresenter.present(
                "1",
                SID,
                URL,
                "requestId",
                0L,
                "<html><body><p>Hello</p></body></html>"
            ) {
                callbackCalled = true
                countDownLatch.countDown()
            }
        }.start()
        countDownLatch.await()

        verify(mockIamDialog, times(0)).show(any<FragmentManager>(), any())
        callbackCalled shouldBe true

    }

    @Test
    fun testPresent_shouldNotShowDialog_whenActivity_isNull() {
        val countDownLatch = CountDownLatch(1)
        var callbackCalled = false
        Thread {
            whenever(mockCurrentActivityProvider.get()).thenReturn(null)

            inAppPresenter.present(
                "1",
                SID,
                URL,
                "requestId",
                0L,
                "<html><body><p>Hello</p></body></html>"
            ) {
                callbackCalled = true
                countDownLatch.countDown()
            }
        }.start()
        countDownLatch.await()

        verify(mockIamDialog, times(0)).show(any<FragmentManager>(), any())
        callbackCalled shouldBe true
    }

    @Test
    fun testPresent_shouldNotShowDialog_whenAnotherDialog_isAlreadyShown() {
        val activity: AppCompatActivity = mock()
        val fragmentManager: FragmentManager = mock()
        val fragment: Fragment = mock()
        val countDownLatch = CountDownLatch(1)
        var callbackCalled = false
        Thread {
            whenever(
                mockIamDialogProvider.provideDialog(
                    anyNotNull(),
                    any(),
                    any(),
                    any()
                )
            ).thenReturn(
                mockIamDialog
            )
            whenever(mockCurrentActivityProvider.get()).thenReturn(activity)
            whenever(activity.supportFragmentManager).thenReturn(fragmentManager)
            whenever(fragmentManager.findFragmentByTag("MOBILE_ENGAGE_IAM_DIALOG_TAG")).thenReturn(
                fragment
            )

            inAppPresenter.present(
                "1",
                SID,
                URL,
                "requestId",
                0L,
                "<html><body><p>Hello</p></body></html>"
            ) {
                callbackCalled = true
                countDownLatch.countDown()
            }
        }.start()
        countDownLatch.await()

        verify(mockIamDialog, times(0)).show(any<FragmentManager>(), any())
        callbackCalled shouldBe true

    }

    @Test
    fun testPresent_shouldNotShowDialog_whenFragmentManager_isInSavedState() {
        val activity: AppCompatActivity = mock()
        val fragmentManager: FragmentManager = mock()
        val countDownLatch = CountDownLatch(1)
        var callbackCalled = false
        Thread {
            whenever(
                mockIamDialogProvider.provideDialog(
                    anyNotNull(),
                    any(),
                    any(),
                    any()
                )
            ).thenReturn(
                mockIamDialog
            )
            whenever(mockCurrentActivityProvider.get()).thenReturn(activity)
            whenever(activity.supportFragmentManager).thenReturn(fragmentManager)
            whenever(fragmentManager.isStateSaved).thenReturn(true)
            whenever(fragmentManager.findFragmentByTag("MOBILE_ENGAGE_IAM_DIALOG_TAG")).thenReturn(
                null
            )

            inAppPresenter.present(
                "1",
                SID,
                URL,
                "requestId",
                0L,
                "<html><body><p>Hello</p></body></html>"
            ) {
                callbackCalled = true
                countDownLatch.countDown()
            }
        }.start()
        countDownLatch.await()

        verify(mockIamDialog, times(0)).show(any<FragmentManager>(), any())
        callbackCalled shouldBe true

    }
}