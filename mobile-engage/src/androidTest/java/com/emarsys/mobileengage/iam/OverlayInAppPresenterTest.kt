package com.emarsys.mobileengage.iam

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.test.rule.ActivityTestRule
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.iam.dialog.IamDialog
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.mockito.anyNotNull
import com.emarsys.testUtil.mockito.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
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
    fun testPresent_shouldShowDialog_whenFragmentActivity_isUsed() {
        whenever(mockCurrentActivityProvider.get()).thenReturn(activityRule.activity as FragmentActivity)

        val countDownLatch = CountDownLatch(1)

        inAppPresenter.present(
            "1",
            SID,
            URL,
            "requestId",
            0L,
            "<html><body><p>Hello</p></body></html>"
        ) {
            countDownLatch.countDown()
        }

        countDownLatch.await()

        verify(mockIamDialog).show(any<FragmentManager>(), any())
    }

    @Test
    fun testPresent_shouldShowDialog_whenAppCompatActivity_isUsed() {
        whenever(mockCurrentActivityProvider.get()).thenReturn(activityRule.activity)

        val countDownLatch = CountDownLatch(1)

        inAppPresenter.present(
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

        verify(mockIamDialog).show(any<FragmentManager>(), any())
    }

    @Test
    fun testPresent_shouldNotShowDialog_whenActivity_isUsed() {
        val activity: Activity = mock()

        whenever(mockCurrentActivityProvider.get()).thenReturn(activity)

        val countDownLatch = CountDownLatch(1)

        inAppPresenter.present(
            "1",
            SID,
            URL,
            "requestId",
            0L,
            "<html><body><p>Hello</p></body></html>"
        ) {
            countDownLatch.countDown()
        }

        countDownLatch.await()

        verify(mockIamDialog, times(0)).show(any<FragmentManager>(), any())
    }

    @Test
    fun testPresent_shouldNotShowDialog_whenActivity_isNull() {
        whenever(mockCurrentActivityProvider.get()).thenReturn(null)

        val countDownLatch = CountDownLatch(1)

        inAppPresenter.present(
            "1",
            SID,
            URL,
            "requestId",
            0L,
            "<html><body><p>Hello</p></body></html>"
        ) {
            countDownLatch.countDown()
        }

        countDownLatch.await()

        verify(mockIamDialog, times(0)).show(any<FragmentManager>(), any())
    }

    @Test
    fun testPresent_shouldNotShowDialog_whenAnotherDialog_isAlreadyShown() {
        val activity: AppCompatActivity = mock()
        val fragmentManager: FragmentManager = mock()
        val fragment: Fragment = mock()

        whenever(mockIamDialogProvider.provideDialog(anyNotNull(), any(), any(), any())).thenReturn(
            mockIamDialog
        )
        whenever(mockCurrentActivityProvider.get()).thenReturn(activity)
        whenever(activity.supportFragmentManager).thenReturn(fragmentManager)
        whenever(fragmentManager.findFragmentByTag("MOBILE_ENGAGE_IAM_DIALOG_TAG")).thenReturn(
            fragment
        )

        val countDownLatch = CountDownLatch(1)

        inAppPresenter.present(
            "1",
            SID,
            URL,
            "requestId",
            0L,
            "<html><body><p>Hello</p></body></html>"
        ) {
            countDownLatch.countDown()
        }

        countDownLatch.await()

        verify(mockIamDialog, times(0)).show(any<FragmentManager>(), any())
    }
}