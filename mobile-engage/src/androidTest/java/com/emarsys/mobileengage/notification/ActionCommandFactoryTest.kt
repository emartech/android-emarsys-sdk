package com.emarsys.mobileengage.notification

import android.content.Context
import android.os.Handler
import com.emarsys.mobileengage.event.EventHandlerProvider
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.notification.command.AppEventCommand
import com.emarsys.mobileengage.notification.command.CustomEventCommand
import com.emarsys.mobileengage.notification.command.OpenExternalUrlCommand
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.mock

class ActionCommandFactoryTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var factory: ActionCommandFactory
    private lateinit var context: Context
    private lateinit var mockUiHandler: Handler
    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockNotificationEventHandlerProvider: EventHandlerProvider

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getTargetContext().applicationContext

        mockEventServiceInternal = mock()
        mockNotificationEventHandlerProvider = mock()
        mockUiHandler = mock()
        factory = ActionCommandFactory(context, mockEventServiceInternal, mockNotificationEventHandlerProvider, mockUiHandler)
    }

    @Test
    fun testCreateActionCommand_shouldReturnAppEventCommand() {
        val result = factory.createActionCommand(JSONObject(mapOf(
                "name" to "name",
                "payload" to mapOf("key" to "value"),
                "type" to "MEAppEvent")))

        result shouldNotBe null
        result!!::class.java shouldBe AppEventCommand::class.java
    }

    @Test
    fun testCreateActionCommand_shouldReturnOpenExternalUrlCommand() {
        val result = factory.createActionCommand(JSONObject(mapOf(
                "url" to "https://emarsys.com",
                "type" to "OpenExternalUrl")))

        result shouldNotBe null
        result!!::class.java shouldBe OpenExternalUrlCommand::class.java
    }

    @Test
    fun testCreateActionCommand_shouldReturnCustomEventCommand() {
        val result = factory.createActionCommand(JSONObject(mapOf(
                "name" to "name",
                "payload" to mapOf("key" to "value"),
                "type" to "MECustomEvent")))

        result shouldNotBe null
        result!!::class.java shouldBe CustomEventCommand::class.java
    }

    @Test
    fun testFindActionWithId_shouldReturnActionWithTheId() {
        val jObj1 = JSONObject(mapOf(
                "id" to "action_id_1",
                "type" to "MEAppEvent"))
        val jObj2 = JSONObject(mapOf(
                "id" to "action_id_2",
                "type" to "MEAppEvent"))
        val jObj3 = JSONObject(mapOf(
                "id" to "action_id_3",
                "payload" to mapOf("key" to "value"),
                "type" to "MECustomEvent"))
        val jObjArray = JSONArray(listOf(jObj1, jObj2, jObj3))

        factory.findActionWithId(jObjArray, "action_id_2") shouldBe jObj2
    }
}