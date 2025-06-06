package com.emarsys.mobileengage.iam

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.test.core.app.ActivityScenario
import com.emarsys.core.activity.TransitionSafeCurrentActivityWatchdog
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.iam.dialog.IamDialog
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import com.emarsys.testUtil.fake.FakeActivity
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class OverlayInAppPresenterTest  {
    companion object {
        private var SID = "testSid"
        private var URL = "https://www.emarsys.com"
    }

    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockIamDialogProvider: IamDialogProvider
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockCurrentActivityProvider: TransitionSafeCurrentActivityWatchdog
    private lateinit var mockIamDialog: IamDialog

    private lateinit var inAppPresenter: OverlayInAppPresenter

    @Before
    fun setUp() {
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockIamDialog = mockk(relaxed = true)
        mockIamDialogProvider = mockk(relaxed = true)
        every {
            mockIamDialogProvider.provideDialog(
                any(),
                any(),
                any(),
                any()
            )
        } returns mockIamDialog

        mockTimestampProvider = mockk(relaxed = true)
        mockCurrentActivityProvider = mockk(relaxed = true)

        every { mockIamDialog.loadInApp(any(), any(), any(), any()) } answers {
            (args[2] as MessageLoadedListener).onMessageLoaded()
        }

        inAppPresenter = OverlayInAppPresenter(
            concurrentHandlerHolder,
            mockIamDialogProvider,
            mockTimestampProvider,
            mockCurrentActivityProvider,
        )
    }

    @Test
    fun testPresent_shouldCallCallback_whenDialogIsShown() {
        val scenario = ActivityScenario.launch(FakeActivity::class.java)
        val countDownLatch = CountDownLatch(1)
        var callbackCalled = false
        Thread {
            scenario.onActivity { activity ->
                every { mockCurrentActivityProvider.activity() } returns activity

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
                every { mockCurrentActivityProvider.activity() } returns activity

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
        verify { mockIamDialog.showNow(any<FragmentManager>(), any()) }
        callbackCalled shouldBe true
    }

    @Test
    fun testPresent_shouldNotShowDialog_whenActivity_isUsed() {
        val activity: Activity = mockk(relaxed = true)
        val countDownLatch = CountDownLatch(1)
        var callbackCalled = false
        Thread {
            every { mockCurrentActivityProvider.activity() } returns activity

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

        verify(exactly = 0) { mockIamDialog.showNow(any<FragmentManager>(), any()) }
        callbackCalled shouldBe true

    }

    @Test
    fun testPresent_shouldNotShowDialog_whenAnotherDialog_isAlreadyShown() {
        val activity: AppCompatActivity = mockk(relaxed = true)
        val fragmentManager: FragmentManager = mockk(relaxed = true)
        val fragment: Fragment = mockk(relaxed = true)
        val countDownLatch = CountDownLatch(1)
        var callbackCalled = false
        Thread {
            every {
                mockIamDialogProvider.provideDialog(
                    any<String>(),
                    any(),
                    any(),
                    any()
                )
            } returns mockIamDialog

            every { mockCurrentActivityProvider.activity() } returns activity
            every { activity.supportFragmentManager } returns fragmentManager
            every { fragmentManager.findFragmentByTag("MOBILE_ENGAGE_IAM_DIALOG_TAG") } returns fragment

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

        verify(exactly = 0) { mockIamDialog.showNow(any<FragmentManager>(), any()) }
        callbackCalled shouldBe true

    }

    @Test
    fun testPresent_shouldNotShowDialog_whenFragmentManager_isInSavedState() {
        val activity: AppCompatActivity = mockk(relaxed = true)
        val fragmentManager: FragmentManager = mockk(relaxed = true)
        val countDownLatch = CountDownLatch(1)
        var callbackCalled = false
        Thread {
            every {
                mockIamDialogProvider.provideDialog(
                    any<String>(),
                    any(),
                    any(),
                    any()
                )
            } returns mockIamDialog

            every { mockCurrentActivityProvider.activity() } returns activity
            every { activity.supportFragmentManager } returns fragmentManager
            every { fragmentManager.isStateSaved } returns true
            every { fragmentManager.findFragmentByTag("MOBILE_ENGAGE_IAM_DIALOG_TAG") } returns null

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

        verify(exactly = 0) { mockIamDialog.showNow(any<FragmentManager>(), any()) }
        callbackCalled shouldBe true
    }
}