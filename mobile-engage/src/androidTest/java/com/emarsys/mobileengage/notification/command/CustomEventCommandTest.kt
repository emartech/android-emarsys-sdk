package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.assertions.throwables.shouldThrow
import org.mockito.Mockito

class CustomEventCommandTest : AnnotationSpec() {
    private lateinit var mockEventServiceInternal: EventServiceInternal

    @Before
    fun setUp() {
        mockEventServiceInternal = Mockito.mock(EventServiceInternal::class.java)
    }

    @Test
    fun testConstructor_mockEventServiceInternal_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            CustomEventCommand(null, "", HashMap())
        }
    }

    @Test
    fun testConstructor_eventName_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            CustomEventCommand(mockEventServiceInternal, null, HashMap())
        }
    }

    @Test
    fun testRun_withEventAttributes() {
        val eventAttributes = HashMap<String, String>()
        eventAttributes["key"] = "value"
        val customEventCommand: Runnable =
            CustomEventCommand(mockEventServiceInternal, EVENT_NAME, eventAttributes)
        customEventCommand.run()
        Mockito.verify(mockEventServiceInternal)
            .trackCustomEventAsync(EVENT_NAME, eventAttributes, null)
    }

    @Test
    fun testRun_withoutEventAttributes() {
        val customEventCommand: Runnable =
            CustomEventCommand(mockEventServiceInternal, EVENT_NAME, null)
        customEventCommand.run()
        Mockito.verify(mockEventServiceInternal).trackCustomEventAsync(EVENT_NAME, null, null)
    }

    companion object {
        private const val EVENT_NAME = "eventName"
    }
}