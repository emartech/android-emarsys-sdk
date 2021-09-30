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
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.*

class IntentUtilsTest {
    private lateinit var context: Context

    @Rule
    @JvmField
    val timeout: TestRule = timeoutRule

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
        val pm : PackageManager = mock()
        val mockActivity: Activity = mock {
            on { packageManager } doReturn pm
            on { packageName } doReturn "packageName"
        }
        val result = createLaunchIntent(Intent(), mockActivity)
        result shouldBe null
    }

    @Test
    fun createNotificationHandlerServiceIntent() {
        val notificationId = 987
        val remoteMessageData: MutableMap<String, String?> = HashMap()
        remoteMessageData["key1"] = "value1"
        remoteMessageData["key2"] = "value2"
        val resultIntent = createNotificationHandlerServiceIntent(
            context,
            remoteMessageData,
            notificationId,
            "action"
        )
        resultIntent.action shouldBe "action"
        val payload = resultIntent.getBundleExtra("payload")
        payload!!.getString("key1") shouldBe "value1"
        payload.getString("key2") shouldBe "value2"
        payload.getInt("notification_id") shouldBe notificationId
    }

    @Test
    fun createNotificationHandlerServiceIntent_withoutAction() {
        val resultIntent = createNotificationHandlerServiceIntent(context, HashMap(), 0, null)
        resultIntent.action shouldBe null
    }
}