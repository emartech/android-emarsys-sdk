package com.emarsys.mobileengage.iam

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.emarsys.core.concurrency.CoreSdkHandler
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.provider.Gettable
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.MobileEngageInternal_V3_Old
import com.emarsys.mobileengage.iam.dialog.IamDialog
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*
import java.util.concurrent.CountDownLatch

class InAppPresenterTest {
    companion object {
        init {
            mock(Fragment::class.java)
            mock(AppCompatActivity::class.java)
        }
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var coreSdkHandler: CoreSdkHandler
    private lateinit var iamWebViewProvider: IamWebViewProvider
    private lateinit var inAppInternal: InAppInternal
    private lateinit var iamDialogProvider: IamDialogProvider
    private lateinit var buttonClickedRepository: Repository<ButtonClicked, SqlSpecification>
    private lateinit var displayedIamRepository: Repository<DisplayedIam, SqlSpecification>
    private lateinit var timestampProvider: TimestampProvider
    private lateinit var mobileEngageInternal: MobileEngageInternal_V3_Old
    private lateinit var activityProvider: Gettable<Activity>
    private lateinit var presenter: InAppPresenter

    @Suppress("UNCHECKED_CAST")
    @Before
    fun setUp() {
        coreSdkHandler = mock(CoreSdkHandler::class.java)
        iamWebViewProvider = IamWebViewProvider(InstrumentationRegistry.getTargetContext())
        inAppInternal = mock(InAppInternal::class.java)
        iamDialogProvider = mock(IamDialogProvider::class.java)
        buttonClickedRepository = mock(Repository::class.java) as Repository<ButtonClicked, SqlSpecification>
        displayedIamRepository = mock(Repository::class.java) as Repository<DisplayedIam, SqlSpecification>
        timestampProvider = mock(TimestampProvider::class.java)
        mobileEngageInternal = mock(MobileEngageInternal_V3_Old::class.java)
        activityProvider = mock(Gettable::class.java) as Gettable<Activity>
        presenter = InAppPresenter(coreSdkHandler,
                iamWebViewProvider,
                inAppInternal,
                iamDialogProvider,
                buttonClickedRepository,
                displayedIamRepository,
                timestampProvider,
                activityProvider
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_coreSdkHandler_mustNotBeNull() {
        InAppPresenter(null,
                iamWebViewProvider,
                inAppInternal,
                iamDialogProvider,
                buttonClickedRepository,
                displayedIamRepository,
                timestampProvider,
                activityProvider
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_iamWebViewProvider_mustNotBeNull() {
        InAppPresenter(coreSdkHandler,
                null,
                inAppInternal,
                iamDialogProvider,
                buttonClickedRepository,
                displayedIamRepository,
                timestampProvider,
                activityProvider
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_inAppInternal_mustNotBeNull() {
        InAppPresenter(coreSdkHandler,
                iamWebViewProvider,
                null,
                iamDialogProvider,
                buttonClickedRepository,
                displayedIamRepository,
                timestampProvider,
                activityProvider
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_iamDialogProvider_mustNotBeNull() {
        InAppPresenter(coreSdkHandler,
                iamWebViewProvider,
                inAppInternal,
                null,
                buttonClickedRepository,
                displayedIamRepository,
                timestampProvider,
                activityProvider
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_buttonClickedRepository_mustNotBeNull() {
        InAppPresenter(coreSdkHandler,
                iamWebViewProvider,
                inAppInternal,
                iamDialogProvider,
                null,
                displayedIamRepository,
                timestampProvider,
                activityProvider
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_displayedIamRepository_mustNotBeNull() {
        InAppPresenter(coreSdkHandler,
                iamWebViewProvider,
                inAppInternal,
                iamDialogProvider,
                buttonClickedRepository,
                null,
                timestampProvider,
                activityProvider
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_timestampProvider_mustNotBeNull() {
        InAppPresenter(coreSdkHandler,
                iamWebViewProvider,
                inAppInternal,
                iamDialogProvider,
                buttonClickedRepository,
                displayedIamRepository,
                null,
                activityProvider
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_activityProvider_mustNotBeNull() {
        InAppPresenter(coreSdkHandler,
                iamWebViewProvider,
                inAppInternal,
                iamDialogProvider,
                buttonClickedRepository,
                displayedIamRepository,
                timestampProvider,
                null
        )
    }

    @Test
    fun testPresent_shouldShowDialog_whenAppCompatActivity_isUsed() {
        val fragmentMock = mock(Fragment::class.java)
        val activityMock = mock(AppCompatActivity::class.java)

        val iamDialog = mock(IamDialog::class.java)
        val fragmentManager = mock(FragmentManager::class.java)

        whenever(activityMock.supportFragmentManager).thenReturn(fragmentManager)
        whenever(fragmentManager.findFragmentById(anyInt())).thenReturn(fragmentMock)
        whenever(activityProvider.get()).thenReturn(activityMock)
        whenever(iamDialogProvider.provideDialog(any(), any())).thenReturn(iamDialog)
        val countDownLatch = CountDownLatch(1)

        presenter.present("1", "requestId", 0L, "<html><body><p>Hello</p></body></html>") {
            countDownLatch.countDown()
        }

        countDownLatch.await()

        verify(iamDialog).show(any<FragmentManager>(), anyString())
    }

    @Test
    fun testPresent_shouldNotShowDialog_whenActivity_isUsed() {
        val iamDialog = mock(IamDialog::class.java)
        val activity = mock(Activity::class.java)

        whenever(activityProvider.get()).thenReturn(activity)
        whenever(iamDialogProvider.provideDialog(any(), any())).thenReturn(iamDialog)

        val countDownLatch = CountDownLatch(1)

        presenter.present("1", "requestId", 0L, "<html><body><p>Hello</p></body></html>") { countDownLatch.countDown() }

        countDownLatch.await()

        verify(iamDialog, times(0)).show(any<FragmentManager>(), any())
    }

    @Test
    fun testPresent_shouldNotShowDialog_whenActivity_isNull() {
        val iamDialog = mock(IamDialog::class.java)

        whenever(activityProvider.get()).thenReturn(null)
        whenever(iamDialogProvider.provideDialog(any(), any())).thenReturn(iamDialog)

        val countDownLatch = CountDownLatch(1)

        presenter.present("1", "requestId", 0L, "<html><body><p>Hello</p></body></html>") { countDownLatch.countDown() }

        countDownLatch.await()

        verify(iamDialog, times(0)).show(any<FragmentManager>(), any())
    }

    @Test
    fun testPresent_shouldNotShowDialog_whenAnotherDialog_isAlreadyShown() {
        val iamDialog = mock(IamDialog::class.java)
        val activity = mock(AppCompatActivity::class.java)
        val fragmentManager = mock(FragmentManager::class.java)
        val fragment = mock(Fragment::class.java)

        whenever(iamDialogProvider.provideDialog(any(), any())).thenReturn(iamDialog)
        whenever(activityProvider.get()).thenReturn(activity)
        whenever(activity.supportFragmentManager).thenReturn(fragmentManager)
        whenever(fragmentManager.findFragmentByTag("MOBILE_ENGAGE_IAM_DIALOG_TAG")).thenReturn(fragment)

        val countDownLatch = CountDownLatch(1)

        presenter.present("1", "requestId", 0L, "<html><body><p>Hello</p></body></html>") { countDownLatch.countDown() }

        countDownLatch.await()

        verify(iamDialog, times(0)).show(any<FragmentManager>(), any())
    }

}