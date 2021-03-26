package com.emarsys

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.rule.ActivityTestRule
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.util.FileDownloader
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.mobileengage.iam.OverlayInAppPresenter
import com.emarsys.mobileengage.service.IntentUtils
import com.emarsys.testUtil.*
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.concurrent.CountDownLatch

class InappNotificationIntegrationTest {

    companion object {
        private const val APP_ID = "14C19-A121F"
        private const val CONTACT_FIELD_ID = 3
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
    val activityRule = ActivityTestRule(FakeActivity::class.java, false, false)

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
                .inAppEventHandler(mock())
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .build()

        FeatureTestUtils.resetFeatures()

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

        DependencyInjection.setup(object : DefaultEmarsysDependencyContainer(baseConfig) {
            override fun getOverlayInAppPresenter(): OverlayInAppPresenter {
                return mockInappPresenterOverlay
            }

            override fun getActivityLifecycleWatchdog(): ActivityLifecycleWatchdog {
                return mock()
            }
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

        context.startService(intent)

        activityRule.launchActivity(Intent())

        completionListenerLatch.await()
    }

}