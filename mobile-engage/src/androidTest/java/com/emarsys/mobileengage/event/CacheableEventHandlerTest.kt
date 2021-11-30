package com.emarsys.mobileengage.event

import android.content.Context
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.mock

internal class CacheableEventHandlerTest {

    private lateinit var cacheableEventHandler: CacheableEventHandler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        cacheableEventHandler = CacheableEventHandler()
    }

    @Test
    fun testHandleEvent_eventsShouldBeEmptyByDefault() {
        val result =
            ReflectionTestUtils.getInstanceField<List<Triple<Context, String, JSONObject>>>(
                cacheableEventHandler,
                "events"
            )

        result shouldBe emptyList()
    }

    @Test
    fun testHandleEvent_shouldStoreEventsInOrder() {
        val mockContext: Context = mock()

        val mockEventName1 = "event1"
        val mockEventName2 = "event2"
        val mockEventName3 = "event3"

        val mockPayload: JSONObject? = null

        val event1 = Triple(mockContext, mockEventName1, mockPayload)
        val event2 = Triple(mockContext, mockEventName2, mockPayload)
        val event3 = Triple(mockContext, mockEventName3, mockPayload)

        cacheableEventHandler.handleEvent(event1.first, event1.second, event1.third)
        cacheableEventHandler.handleEvent(event2.first, event2.second, event2.third)
        cacheableEventHandler.handleEvent(event3.first, event3.second, event3.third)

        val result =
            ReflectionTestUtils.getInstanceField<List<Triple<Context, String, JSONObject>>>(
                cacheableEventHandler,
                "events"
            )

        result shouldNotBe null
        result!!.let {
            result[0] shouldBe event1
            result[1] shouldBe event2
            result[2] shouldBe event3
        }
    }

    @Test
    fun testHandleEvent_shouldNotStoreEvents_whenEventHandlerIsSet() {
        cacheableEventHandler.eventHandler = EventHandler { _, _, _ -> }
        val mockContext: Context = mock()

        val mockEventName1 = "event1"
        val mockEventName2 = "event2"
        val mockEventName3 = "event3"

        val mockPayload: JSONObject? = null

        val event1 = Triple(mockContext, mockEventName1, mockPayload)
        val event2 = Triple(mockContext, mockEventName2, mockPayload)
        val event3 = Triple(mockContext, mockEventName3, mockPayload)

        cacheableEventHandler.handleEvent(event1.first, event1.second, event1.third)
        cacheableEventHandler.handleEvent(event2.first, event2.second, event2.third)
        cacheableEventHandler.handleEvent(event3.first, event3.second, event3.third)

        val result =
            ReflectionTestUtils.getInstanceField<List<Triple<Context, String, JSONObject>>>(
                cacheableEventHandler,
                "events"
            )

        result shouldBe emptyList()
    }

    @Test
    fun testHandleEvent_shouldCallEventHandler_withEvents_whenEventHandlerRegistered() {
        val triggeredEvents = mutableListOf<Triple<Context, String, JSONObject?>>()
        cacheableEventHandler.eventHandler = EventHandler { context, name, payload ->
            triggeredEvents.add(Triple(context, name, payload))
        }
        val mockContext: Context = mock()

        val mockEventName1 = "event1"
        val mockEventName2 = "event2"
        val mockEventName3 = "event3"

        val mockPayload: JSONObject? = null

        val event1 = Triple(mockContext, mockEventName1, mockPayload)
        val event2 = Triple(mockContext, mockEventName2, mockPayload)
        val event3 = Triple(mockContext, mockEventName3, mockPayload)

        cacheableEventHandler.handleEvent(event1.first, event1.second, event1.third)
        cacheableEventHandler.handleEvent(event2.first, event2.second, event2.third)
        cacheableEventHandler.handleEvent(event3.first, event3.second, event3.third)

        val result =
            ReflectionTestUtils.getInstanceField<List<Triple<Context, String, JSONObject>>>(
                cacheableEventHandler,
                "events"
            )

        result shouldBe emptyList()
        triggeredEvents shouldNotBe null
        triggeredEvents.let {
            triggeredEvents[0] shouldBe event1
            triggeredEvents[1] shouldBe event2
            triggeredEvents[2] shouldBe event3
        }
    }

    @Test
    fun testHandleEvent_shouldTriggerStoredEvents_whenEventHandlerRegistered() {
        val triggeredEvents = mutableListOf<Triple<Context, String, JSONObject?>>()
        val mockContext: Context = mock()

        val mockEventName1 = "event1"
        val mockEventName2 = "event2"
        val mockEventName3 = "event3"

        val mockPayload: JSONObject? = null

        val event1 = Triple(mockContext, mockEventName1, mockPayload)
        val event2 = Triple(mockContext, mockEventName2, mockPayload)
        val event3 = Triple(mockContext, mockEventName3, mockPayload)

        cacheableEventHandler.handleEvent(event1.first, event1.second, event1.third)
        cacheableEventHandler.handleEvent(event2.first, event2.second, event2.third)
        cacheableEventHandler.handleEvent(event3.first, event3.second, event3.third)

        val result =
            ReflectionTestUtils.getInstanceField<List<Triple<Context, String, JSONObject>>>(
                cacheableEventHandler,
                "events"
            )

        result!!.let {
            result[0] shouldBe event1
            result[1] shouldBe event2
            result[2] shouldBe event3
        }
        triggeredEvents shouldBe emptyList()

        cacheableEventHandler.eventHandler = EventHandler { context, name, payload ->
            triggeredEvents.add(Triple(context, name, payload))
        }

        val resultAfterEventHandlerIsSet =
            ReflectionTestUtils.getInstanceField<List<Triple<Context, String, JSONObject>>>(
                cacheableEventHandler,
                "events"
            )

        resultAfterEventHandlerIsSet shouldBe emptyList()
        triggeredEvents.let {
            triggeredEvents[0] shouldBe event1
            triggeredEvents[1] shouldBe event2
            triggeredEvents[2] shouldBe event3
        }
    }
}