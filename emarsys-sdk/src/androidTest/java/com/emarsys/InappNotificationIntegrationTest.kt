package com.emarsys

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.app.AppLifecycleObserver
import com.emarsys.core.util.FileDownloader
import com.emarsys.di.DefaultEmarsysComponent
import com.emarsys.di.DefaultEmarsysDependencies
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.iam.OverlayInAppPresenter
import com.emarsys.mobileengage.service.IntentUtils
import com.emarsys.testUtil.*
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*
import java.util.concurrent.CountDownLatch

class InappNotificationIntegrationTest {

    companion object {
        private const val APP_ID = "14C19-A121F"
    }

    private lateinit var completionListenerLatch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig
    private lateinit var mockInappPresenterOverlay: OverlayInAppPresenter

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    var activityScenarioRule: ActivityScenarioRule<FakeActivity> = ActivityScenarioRule(FakeActivity::class.java)

    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

    @Before
    fun setup() {
        completionListenerLatch = CountDownLatch(1)

        DatabaseTestUtils.deleteCoreDatabase()

        application.getSharedPreferences("emarsys_secret_shared_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()

        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .applicationCode(APP_ID)
                .build()

        mockInappPresenterOverlay = mock()

        whenever(mockInappPresenterOverlay.present(
                anyOrNull(),
                eq(null),
                anyOrNull(),
                eq(null),
                anyOrNull(),
                anyOrNull(),
                eq(null))).thenAnswer {
            completionListenerLatch.countDown()
        }

        DefaultEmarsysDependencies(baseConfig, object : DefaultEmarsysComponent(baseConfig) {
            override val overlayInAppPresenter: OverlayInAppPresenter
                get() = mockInappPresenterOverlay
            override val activityLifecycleWatchdog: ActivityLifecycleWatchdog
                get() = mock()
            override val appLifecycleObserver: AppLifecycleObserver
                get() = mock()
            override val eventServiceInternal: EventServiceInternal
                get() = mock()
        })

        ConnectionTestUtils.checkConnection(application)

        Emarsys.setup(baseConfig)

        IntegrationTestUtils.doLogin()
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys(application)
    }

    @Test
    fun testInappPresent() {
        val context = InstrumentationRegistry.getTargetContext().applicationContext
        val url = FileDownloader(context).download("https://www.google.com")
        val emsPayload = """{"inapp": {"campaignId": "222","url": "https://www.google.com","fileUrl": "$url"}}"""
        val remoteMessageData = mapOf("ems" to emsPayload)

        val intent = IntentUtils.createNotificationHandlerServiceIntent(
                context,
                remoteMessageData,
                0,
                null
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)

        activityScenarioRule.scenario.moveToState(Lifecycle.State.CREATED)

        completionListenerLatch.await()
        verify(mockInappPresenterOverlay).present(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

}