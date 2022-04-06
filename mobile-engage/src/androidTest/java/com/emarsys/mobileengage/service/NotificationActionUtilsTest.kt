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
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.ThreadSpy
import io.kotlintest.shouldBe
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch

class NotificationActionUtilsTest {
    private lateinit var context: Context

    @Rule
    @JvmField
    var timeout: TestRule = TimeoutUtils.timeoutRule

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
        val factory = Mockito.mock(NotificationCommandFactory::class.java)
        val intent = Mockito.mock(Intent::class.java)
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
        val ems = JSONObject().put("actions",
                JSONArray().put(JSONObject()
                        .put("title", "title")
                        .put("type", "MEAppEvent")
                ))
        val input: MutableMap<String, String> = HashMap()
        input["ems"] = ems.toString()
        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(context, input, 0)
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun testCreateActions_missingTitle() {
        val ems = JSONObject().put("actions",
                JSONArray().put(JSONObject()
                        .put("id", "uniqueActionId")
                        .put("type", "MEAppEvent")
                ))
        val input: MutableMap<String, String> = HashMap()
        input["ems"] = ems.toString()
        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(context, input, 0)
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun testCreateActions_missingType() {
        val ems = JSONObject().put("actions",
                JSONArray().put(JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                ))
        val input: MutableMap<String, String> = HashMap()
        input["ems"] = ems.toString()
        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(context, input, 0)
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun testCreateActions_appEvent_missingEventName() {
        val ems = JSONObject().put("actions",
                JSONArray().put(JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                        .put("type", "MEAppEvent")
                ))
        val input: MutableMap<String, String> = HashMap()
        input["ems"] = ems.toString()
        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(context, input, 0)
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    @Throws(JSONException::class)
    fun testCreateActions_appEvent_withSingleAction() {
        val payload = JSONObject()
                .put("actions", JSONArray()
                        .put(JSONObject()
                                .put("id", "uniqueActionId")
                                .put("title", "Action button title")
                                .put("type", "MEAppEvent")
                                .put("name", "Name of the event")
                                .put("payload", JSONObject()
                                        .put("payloadKey", "payloadValue")))
                )
        val input: Map<String, String> = mapOf(
                "ems" to payload.toString()
        )
        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(context, input, 0)
        result.size shouldBe 1
        result[0].title shouldBe "Action button title"
    }

    @Test
    @Throws(JSONException::class)
    fun testCreateActions_appEvent_withMultipleActions() {
        val payload = JSONObject()
                .put("actions", JSONArray()
                        .put(JSONObject()
                                .put("id", "uniqueActionId1")
                                .put("title", "title1")
                                .put("type", "MEAppEvent")
                                .put("name", "event1")
                        )
                        .put(JSONObject()
                                .put("id", "uniqueActionId2")
                                .put("title", "title2")
                                .put("type", "MEAppEvent")
                                .put("name", "event2")
                                .put("payload", JSONObject()
                                        .put("payloadKey", "payloadValue"))
                        ))
        val input: MutableMap<String, String> = HashMap()
        input["ems"] = payload.toString()
        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(context, input, 0)
        result.size shouldBe 2
        result[0].title shouldBe "title1"
        result[1].title shouldBe "title2"
    }

    @Test
    @Throws(JSONException::class)
    fun testCreateActions_externalUrl_missingUrl() {
        val ems = JSONObject().put("actions",
                JSONArray().put(JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                        .put("type", "OpenExternalUrl")
                ))
        val input: MutableMap<String, String> = HashMap()
        input["ems"] = ems.toString()
        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(context, input, 0)
        result.size shouldBe 0
    }

    @Test
    @Throws(JSONException::class)
    fun testCreateActions_externalUrl_withSingleAction() {
        val payload = JSONObject()
                .put("actions", JSONArray()
                        .put(JSONObject()
                                .put("id", "uniqueActionId")
                                .put("title", "Action button title")
                                .put("type", "OpenExternalUrl")
                                .put("url", "https://www.emarsys.com")
                        )
                )
        val input: MutableMap<String, String> = HashMap()
        input["ems"] = payload.toString()
        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(context, input, 0)
        result.size shouldBe 1
        result[0].title shouldBe "Action button title"
    }

    @Test
    @Throws(JSONException::class)
    fun testCreateActions_externalUrl_withMultipleActions() {
        val payload = JSONObject()
                .put("actions", JSONArray()
                        .put(JSONObject()
                                .put("id", "uniqueActionId")
                                .put("title", "Action button title")
                                .put("type", "OpenExternalUrl")
                                .put("url", "https://www.emarsys.com")
                        )
                        .put(JSONObject()
                                .put("id", "uniqueActionId2")
                                .put("title", "Second button title")
                                .put("type", "OpenExternalUrl")
                                .put("url", "https://www.emarsys/faq.com")
                        )
                )
        val input: MutableMap<String, String> = HashMap()
        input["ems"] = payload.toString()
        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(context, input, 0)
        result.size shouldBe 2
        result[0].title shouldBe "Action button title"
        result[1].title shouldBe "Second button title"
    }

    @Test
    @Throws(JSONException::class)
    fun testCreateActions_customEvent_missingName() {
        val ems = JSONObject().put("actions",
                JSONArray().put(JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                        .put("type", "MECustomEvent")
                ))
        val input: MutableMap<String, String> = HashMap()
        input["ems"] = ems.toString()
        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(context, input, 0)
        result.size shouldBe 0
    }


    @Test
    @Throws(JSONException::class)
    fun testCreateActions_customEvent_withSingleAction() {
        val ems = JSONObject().put("actions",
                JSONArray().put(JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                        .put("type", "MECustomEvent")
                        .put("name", "eventName")
                        .put("payload", JSONObject()
                                .put("key1", "value1")
                                .put("key2", "value2"))
                ))
        val input: MutableMap<String, String> = HashMap()
        input["ems"] = ems.toString()
        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(context, input, 0)
        result.size shouldBe 1
        result[0].title shouldBe "Action button title"
    }

    @Test
    @Throws(JSONException::class)
    fun testCreateActions_customEvent_withSingleAction_withoutPayload() {
        val ems = JSONObject().put("actions",
                JSONArray().put(JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                        .put("type", "MECustomEvent")
                        .put("name", "eventName")
                ))
        val input: MutableMap<String, String> = HashMap()
        input["ems"] = ems.toString()
        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(context, input, 0)
        result.size shouldBe 1
        result[0].title shouldBe "Action button title"
    }

    @Test
    @Throws(JSONException::class)
    fun testCreateActions_customEvent_withMultipleActions() {
        val ems = JSONObject().put("actions",
                JSONArray().put(JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                        .put("type", "MECustomEvent")
                        .put("name", "eventName")
                        .put("payload", JSONObject()
                                .put("key1", "value1")
                                .put("key2", "value2")))
                        .put(JSONObject()
                                .put("id", "uniqueActionId2")
                                .put("title", "Another button title")
                                .put("type", "MECustomEvent")
                                .put("name", "eventName")
                        ))
        val input: MutableMap<String, String> = HashMap()
        input["ems"] = ems.toString()
        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(context, input, 0)

        result.size shouldBe 2
        result[0].title shouldBe "Action button title"
        result[1].title shouldBe "Another button title"
    }

    @Test
    @Throws(Exception::class)
    fun testCreateActions_dismiss() {
        val ems = JSONObject().put("actions",
                JSONArray().put(JSONObject()
                        .put("id", "uniqueActionId")
                        .put("title", "Action button title")
                        .put("type", "Dismiss")
                ))
        val input: MutableMap<String, String> = HashMap()
        input["ems"] = ems.toString()
        val result: List<NotificationCompat.Action> = NotificationActionUtils.createActions(context, input, 0)
        result.size shouldBe 1
        result[0].title shouldBe "Action button title"
    }
}