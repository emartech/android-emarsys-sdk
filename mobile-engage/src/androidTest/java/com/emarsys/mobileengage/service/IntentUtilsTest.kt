package com.emarsys.mobileengage.service

import android.app.Activity
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.emarsys.core.util.AndroidVersionUtils.isBelowUpsideDownCake
import com.emarsys.core.util.AndroidVersionUtils.isUpsideDownCakeOrHigher
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.service.IntentUtils.createLaunchPendingIntent
import com.emarsys.mobileengage.service.IntentUtils.createNotificationHandlerServiceIntent
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class IntentUtilsTest  {
    private companion object {
        const val BACKGROUND_ACTIVITY_START_MODE_KEY = "android.activity.pendingIntentCreatorBackgroundActivityStartMode"
        const val TITLE = "title"
        const val BODY = "body"
        const val CHANNEL_ID = "channelId"
        const val COLLAPSE_ID = "testCollapseId"
        const val MULTICHANNEL_ID = "test multiChannel id"
        const val SID = "test sid"
        const val SMALL_RESOURCE_ID = 123
        const val COLOR_RESOURCE_ID = 456
        val notificationData = NotificationData(
            null,
            null,
            null,
            TITLE,
            BODY,
            CHANNEL_ID,
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_RESOURCE_ID,
            colorResourceId = COLOR_RESOURCE_ID,
            collapseId = COLLAPSE_ID,
            operation = NotificationOperation.INIT.name,
            actions = null,
            inapp = null,
            u = "{\"customField\":\"customValue\"}",
            message_id = "messageId"
        )
    }

    private lateinit var context: Context
    private lateinit var launchIntentForPackage: Intent
    private lateinit var mockPackageManager: PackageManager
    private lateinit var mockActivity: Activity
    private val bundleCaptor = slot<Bundle>()
    private val intentCaptor = slot<Intent>()

    @Before
    fun init() {
        setupMobileEngageComponent(FakeMobileEngageDependencyContainer())
        context = getTargetContext()
        mockkStatic(PendingIntent::class)

        launchIntentForPackage = Intent()
        mockPackageManager = mockk(relaxed = true)
        every { mockPackageManager.getLaunchIntentForPackage(any()) } returns launchIntentForPackage

        mockActivity = mockk(relaxed = true)
        every { mockActivity.packageManager } returns mockPackageManager
        every { mockActivity.packageName } returns "com.emarsys.mobileengage.test"
        every {
            PendingIntent.getActivity(
                mockActivity,
                0,
                launchIntentForPackage,
                any(),
                any()
            )
        } returns mockk()
    }

    @After
    fun tearDown() {
        unmockkStatic(PendingIntent::class)
        bundleCaptor.clear()
        intentCaptor.clear()
    }

    @Test
    fun testCreateLaunchIntent() {
        if(isBelowUpsideDownCake) {
            val remoteIntent = Intent()
            val expectedExtras = Bundle()
            expectedExtras.putString("key", "value")
            remoteIntent.putExtras(expectedExtras)

            createLaunchPendingIntent(remoteIntent, mockActivity)

            verify {
                PendingIntent.getActivity(
                    mockActivity,
                    0,
                    capture(intentCaptor),
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
                    null
                )
            }

            intentCaptor.captured.extras?.getString("key") shouldBe "value"
        }
    }

    @Test
    fun testCreateLaunchIntent_shouldCreatePendingIntentWithApplicationOptionsAboveApiLevel34() {
        if(isUpsideDownCakeOrHigher) {
            val remoteIntent = Intent()
            val expectedExtras = Bundle()
            expectedExtras.putString("key", "value")
            remoteIntent.putExtras(expectedExtras)

            createLaunchPendingIntent(remoteIntent, mockActivity)

            verify {
                PendingIntent.getActivity(
                    mockActivity,
                    0,
                    capture(intentCaptor),
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
                    capture(bundleCaptor)
                )
            }

            bundleCaptor.captured.getInt(BACKGROUND_ACTIVITY_START_MODE_KEY) shouldBe 1
            intentCaptor.captured.extras?.getString("key") shouldBe "value"
        }
    }

    @Test
    fun testCreateLaunchIntent_withNoBundleInIntent() {
        if (isBelowUpsideDownCake) {
            createLaunchPendingIntent(Intent(), mockActivity)
            launchIntentForPackage.extras shouldBe null

            verify {
                PendingIntent.getActivity(
                    mockActivity,
                    0,
                    capture(intentCaptor),
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
                    null
                )
            }

            intentCaptor.captured.extras shouldBe null
        }
    }

    @Test
    fun testCreateLaunchIntent_withNoBundleInIntent_aboveApiLevel34() {
        if (isUpsideDownCakeOrHigher) {
            createLaunchPendingIntent(Intent(), mockActivity)
            launchIntentForPackage.extras shouldBe null

            verify {
                PendingIntent.getActivity(
                    mockActivity,
                    0,
                    capture(intentCaptor),
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
                    capture(bundleCaptor)
                )
            }

            bundleCaptor.captured.getInt(BACKGROUND_ACTIVITY_START_MODE_KEY) shouldBe 1
            intentCaptor.captured.extras shouldBe null
        }
    }

    @Test
    fun testCreateLaunchIntent_whenLaunchIntentForPackageIsNull() {
        every { mockPackageManager.getLaunchIntentForPackage(any()) } returns null
        val result = createLaunchPendingIntent(Intent(), mockActivity)
        result shouldBe null
    }

    @Test
    fun testCreateNotificationHandlerServiceIntent() {
        val resultIntent = createNotificationHandlerServiceIntent(
            context,
            notificationData,
            "action"
        )
        resultIntent.action shouldBe "action"
        val payload = resultIntent.getParcelableExtra<NotificationData>("payload")
        payload shouldBe notificationData
    }

    @Test
    fun createNotificationHandlerServiceIntent_withoutAction() {
        val resultIntent = createNotificationHandlerServiceIntent(
            context,
            notificationData,
            null
        )
        resultIntent.action shouldBe null
    }
}