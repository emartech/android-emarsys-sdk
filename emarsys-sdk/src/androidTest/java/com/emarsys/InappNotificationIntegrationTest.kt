package com.emarsys

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.activity.ActivityLifecycleActionRegistry
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.app.AppLifecycleObserver
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.util.FileDownloader
import com.emarsys.di.DefaultEmarsysComponent
import com.emarsys.di.DefaultEmarsysDependencies
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.iam.OverlayInAppPresenter
import com.emarsys.mobileengage.service.IntentUtils
import com.emarsys.mobileengage.service.NotificationData
import com.emarsys.mobileengage.service.NotificationOperation
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.ConnectionTestUtils
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.TestUrls.LARGE_IMAGE
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.CountDownLatch

class InappNotificationIntegrationTest : AnnotationSpec() {

    private companion object {
        const val APP_ID = "14C19-A121F"
        const val SID = "129487fw123"
    }

    private lateinit var completionListenerLatch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig
    private lateinit var mockInappPresenterOverlay: OverlayInAppPresenter
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application


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

        mockInappPresenterOverlay = mockk(relaxed = true)

        every {
            mockInappPresenterOverlay.present(
                any(),
                SID,
                any(),
                null,
                any(),
                any(),
                null
            )
        } answers {
            completionListenerLatch.countDown()
        }
        mockCurrentActivityProvider = mockk(relaxed = true)
        val mockActivity = mockk<Activity>(relaxed = true)
        every { mockCurrentActivityProvider.get() } returns mockActivity

        DefaultEmarsysDependencies(baseConfig, object : DefaultEmarsysComponent(baseConfig) {
            override val overlayInAppPresenter: OverlayInAppPresenter
                get() = mockInappPresenterOverlay
            override val activityLifecycleWatchdog: ActivityLifecycleWatchdog
                get() = mockk(relaxed = true)
            override val currentActivityProvider: CurrentActivityProvider
                get() = mockCurrentActivityProvider
            override val activityLifecycleActionRegistry: ActivityLifecycleActionRegistry
                get() = mockk(relaxed = true)
            override val appLifecycleObserver: AppLifecycleObserver
                get() = mockk(relaxed = true)
            override val eventServiceInternal: EventServiceInternal
                get() = mockk(relaxed = true)
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
        val url =
            FileDownloader(application).download(LARGE_IMAGE)
        val inappPayload =
            """{"campaignId": "222","url": "$LARGE_IMAGE","fileUrl": "$url"}"""
        val notificationData = NotificationData(
            null,
            null,
            null,
            "title",
            "body",
            "channelId",
            campaignId = "test multiChannel id",
            sid = SID,
            smallIconResourceId = 123,
            colorResourceId = 456,
            collapseId = "collapseId",
            operation = NotificationOperation.INIT.name,
            actions = null,
            defaultAction = null,
            inapp = inappPayload,
            u = "{\"customField\":\"customValue\"}",
            message_id = "messageId"
        )
        val intent = IntentUtils.createNotificationHandlerServiceIntent(
            application,
            notificationData,
            null
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        application.startActivity(intent)

        completionListenerLatch.await()
        verify {
            mockInappPresenterOverlay.present(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
    }
}