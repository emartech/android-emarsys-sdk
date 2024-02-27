package com.emarsys.mobileengage.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.service.IntentUtils.createLaunchIntent
import com.emarsys.mobileengage.service.IntentUtils.createNotificationHandlerServiceIntent
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class IntentUtilsTest : AnnotationSpec() {
    private companion object {
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
            inapp = null
        )
    }

    private lateinit var context: Context

    @Before
    fun init() {
        setupMobileEngageComponent(FakeMobileEngageDependencyContainer())
        context = getTargetContext()
    }

    @Test
    fun testCreateLaunchIntent() {
        val launchIntentForPackage = Intent()
        val pm: PackageManager = mock {
            on { getLaunchIntentForPackage(any()) } doReturn launchIntentForPackage
        }
        val mockActivity: Activity = mock {
            on { packageManager } doReturn pm
            on { packageName } doReturn "packageName"
        }
        val intent = Intent()
        val bundle = Bundle()
        bundle.putString("key", "value")
        intent.putExtras(bundle)
        val result = createLaunchIntent(intent, mockActivity)
        launchIntentForPackage shouldBe result
        val launcherBundle = launchIntentForPackage.extras
        bundle.keySet() shouldBe launcherBundle!!.keySet()
        for (key in bundle.keySet()) {
            bundle[key] shouldBe launcherBundle[key]
        }
    }

    @Test
    fun testCreateLaunchIntent_withNoBundleInIntent() {
        val launchIntentForPackage = Intent()
        val pm: PackageManager = mock {
            on { getLaunchIntentForPackage(any()) } doReturn launchIntentForPackage
        }
        val mockActivity: Activity = mock {
            on { packageManager } doReturn pm
            on { packageName } doReturn "packageName"
        }
        val result = createLaunchIntent(Intent(), mockActivity)
        launchIntentForPackage shouldBe result
        launchIntentForPackage.extras shouldBe null
    }

    @Test
    fun testCreateLaunchIntent_whenIntentIsNull() {
        val pm: PackageManager = mock()
        val mockActivity: Activity = mock {
            on { packageManager } doReturn pm
            on { packageName } doReturn "packageName"
        }
        val result = createLaunchIntent(Intent(), mockActivity)
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