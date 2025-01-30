package com.emarsys.mobileengage.service


import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.notification.NotificationCommandFactory
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.mockito.ThreadSpy
import io.kotest.matchers.shouldBe
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch

class NotificationActionUtilsTest  {
    private companion object {
        const val SMALL_RESOURCE_ID = 123
        const val COLOR_RESOURCE_ID = 456
        const val SID = "test sid"
        const val MULTICHANNEL_ID = "test multichannel id"
        const val COLLAPSE_ID = "testCollapseId"
        val OPERATION = NotificationOperation.INIT.name
        val testNotificationData = NotificationData(
            campaignId = MULTICHANNEL_ID,
            sid = SID,
            smallIconResourceId = SMALL_RESOURCE_ID,
            colorResourceId = COLOR_RESOURCE_ID,
            collapseId = COLLAPSE_ID,
            operation = OPERATION,
            actions = null,
            inapp = null,
            u = "{\"customField\":\"customValue\"}",
            message_id = "messageId"
        )
    }

    private lateinit var context: Context

    @Before
    fun init() {
        context = getTargetContext().applicationContext
        setupMobileEngageComponent(FakeMobileEngageDependencyContainer())
    }

    @After
    fun tearDown() {
        mobileEngage().concurrentHandlerHolder.coreLooper.quitSafely()
        tearDownMobileEngageComponent()
    }

    @Test
    fun testHandleAction_runsNotificationCommand() {
        val threadSpy = ThreadSpy<Unit>()
        val latch = CountDownLatch(1)
        val factory = Mockito.mock(NotificationCommandFactory::
class.java)
        val intent = Mockito.mock(Intent::
class.java)
        val command = Runnable {
            threadSpy.call()
            latch.countDown()
        }
        whenever(factory.createNotificationCommand(intent)).thenReturn(command)
        NotificationActionUtils.handleAction(intent, factory)

        latch.await()
        threadSpy.verifyCalledOnCoreSdkThread()
    }

    @Test
    fun testCreateActions_missingId() {
        val testActions =
            JSONArray().put(
                JSONObject()
                    .put("title", "title")
                    .put("type", "MEAppEvent")
            )

        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(
            context,
            testActions,
            testNotificationData
        )
        result.isEmpty() shouldBe true
    }

    @Test
    fun testCreateActions_missingTitle() {
        val testActions = JSONArray().put(
            JSONObject()
                .put("id", "uniqueActionId")
                .put("type", "MEAppEvent")
        )

        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(
            context,
            testActions,
            testNotificationData
        )
        result.isEmpty() shouldBe true
    }

    @Test
    fun testCreateActions_missingType() {
        val testActions = JSONArray().put(
            JSONObject()
                .put("id", "uniqueActionId")
                .put("title", "Action button title")
        )

        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(
            context,
            testActions,
            testNotificationData
        )
        result.isEmpty() shouldBe true
    }

    @Test
    fun testCreateActions_appEvent_missingEventName() {
        val testActions = JSONArray().put(
            JSONObject()
                .put("id", "uniqueActionId")
                .put("title", "Action button title")
                .put("type", "MEAppEvent")
        )

        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(
            context,
            testActions,
            testNotificationData
        )
        result.isEmpty() shouldBe true
    }

    @Test
    @Throws(JSONException::
class)
    fun testCreateActions_appEvent_withSingleAction() {
        val testActions = JSONArray()
            .put(
                JSONObject()
                    .put("id", "uniqueActionId")
                    .put("title", "Action button title")
                    .put("type", "MEAppEvent")
                    .put("name", "Name of the event")
                    .put(
                        "payload", JSONObject()
                            .put("payloadKey", "payloadValue")
                    )
            )


        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(
            context,
            testActions,
            testNotificationData
        )
        result.size shouldBe 1
        result[0].title shouldBe "Action button title"
    }

    @Test
    @Throws(JSONException::
class)
    fun testCreateActions_appEvent_withMultipleActions() {
        val payload = JSONArray()
            .put(
                JSONObject()
                    .put("id", "uniqueActionId1")
                    .put("title", "title1")
                    .put("type", "MEAppEvent")
                    .put("name", "event1")
            )
            .put(
                JSONObject()
                    .put("id", "uniqueActionId2")
                    .put("title", "title2")
                    .put("type", "MEAppEvent")
                    .put("name", "event2")
                    .put(
                        "payload", JSONObject()
                            .put("payloadKey", "payloadValue")
                    )
            )

        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(
            context,
            payload,
            testNotificationData
        )
        result.size shouldBe 2
        result[0].title shouldBe "title1"
        result[1].title shouldBe "title2"
    }

    @Test
    @Throws(JSONException::
class)
    fun testCreateActions_externalUrl_missingUrl() {
        val testActions = JSONArray().put(
            JSONObject()
                .put("id", "uniqueActionId")
                .put("title", "Action button title")
                .put("type", "OpenExternalUrl")
        )

        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(
            context,
            testActions,
            testNotificationData
        )
        result.size shouldBe 0
    }

    @Test
    @Throws(JSONException::
class)
    fun testCreateActions_externalUrl_withSingleAction() {
        val payload = JSONArray()
            .put(
                JSONObject()
                    .put("id", "uniqueActionId")
                    .put("title", "Action button title")
                    .put("type", "OpenExternalUrl")
                    .put("url", "https://www.emarsys.com")
            )

        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(
            context,
            payload,
            testNotificationData
        )
        result.size shouldBe 1
        result[0].title shouldBe "Action button title"
    }

    @Test
    @Throws(JSONException::
class)
    fun testCreateActions_externalUrl_withMultipleActions() {
        val payload = JSONArray()
            .put(
                JSONObject()
                    .put("id", "uniqueActionId")
                    .put("title", "Action button title")
                    .put("type", "OpenExternalUrl")
                    .put("url", "https://www.emarsys.com")
            )
            .put(
                JSONObject()
                    .put("id", "uniqueActionId2")
                    .put("title", "Second button title")
                    .put("type", "OpenExternalUrl")
                    .put("url", "https://www.emarsys/faq.com")
            )


        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(
            context,
            payload,
            testNotificationData
        )
        result.size shouldBe 2
        result[0].title shouldBe "Action button title"
        result[1].title shouldBe "Second button title"
    }

    @Test
    @Throws(JSONException::
class)
    fun testCreateActions_customEvent_missingName() {
        val testActions = JSONArray().put(
            JSONObject()
                .put("id", "uniqueActionId")
                .put("title", "Action button title")
                .put("type", "MECustomEvent")
        )


        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(
            context,
            testActions,
            testNotificationData
        )
        result.size shouldBe 0
    }

    @Test
    @Throws(JSONException::
class)
    fun testCreateActions_customEvent_withSingleAction() {
        val testActions = JSONArray().put(
            JSONObject()
                .put("id", "uniqueActionId")
                .put("title", "Action button title")
                .put("type", "MECustomEvent")
                .put("name", "eventName")
                .put(
                    "payload", JSONObject()
                        .put("key1", "value1")
                        .put("key2", "value2")
                )
        )

        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(
            context,
            testActions,
            testNotificationData
        )
        result.size shouldBe 1
        result[0].title shouldBe "Action button title"
    }

    @Test
    @Throws(JSONException::
class)
    fun testCreateActions_customEvent_withSingleAction_withoutPayload() {
        val testActions = JSONArray().put(
            JSONObject()
                .put("id", "uniqueActionId")
                .put("title", "Action button title")
                .put("type", "MECustomEvent")
                .put("name", "eventName")
        )

        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(
            context,
            testActions,
            testNotificationData
        )
        result.size shouldBe 1
        result[0].title shouldBe "Action button title"
    }

    @Test
    @Throws(JSONException::
class)
    fun testCreateActions_customEvent_withMultipleActions() {
        val testActions = JSONArray().put(
            JSONObject()
                .put("id", "uniqueActionId")
                .put("title", "Action button title")
                .put("type", "MECustomEvent")
                .put("name", "eventName")
                .put(
                    "payload", JSONObject()
                        .put("key1", "value1")
                        .put("key2", "value2")
                )
        )
            .put(
                JSONObject()
                    .put("id", "uniqueActionId2")
                    .put("title", "Another button title")
                    .put("type", "MECustomEvent")
                    .put("name", "eventName")
            )

        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(
            context,
            testActions,
            testNotificationData
        )

        result.size shouldBe 2
        result[0].title shouldBe "Action button title"
        result[1].title shouldBe "Another button title"
    }

    @Test
    @Throws(Exception::
class)
    fun testCreateActions_dismiss() {
        val testActions = JSONArray().put(
            JSONObject()
                .put("id", "uniqueActionId")
                .put("title", "Action button title")
                .put("type", "Dismiss")
        )

        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(
            context,
            testActions,
            testNotificationData
        )
        result.size shouldBe 1
        result[0].title shouldBe "Action button title"
    }
}