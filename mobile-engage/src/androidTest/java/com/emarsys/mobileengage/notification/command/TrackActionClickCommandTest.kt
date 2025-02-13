package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.event.EventServiceInternal
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class TrackActionClickCommandTest {
    private lateinit var mockEventServiceInternal: EventServiceInternal

    @Before
    fun setup() {
        mockEventServiceInternal = mockk(relaxed = true)
    }

    @Test
    fun testRun_sendsInternalCustomEvent() {
        val buttonId = "buttonId"
        val sid = "sid1234"
        TrackActionClickCommand(mockEventServiceInternal, buttonId, sid).run()
        val payload: MutableMap<String, String> = HashMap()
        payload["button_id"] = buttonId
        payload["origin"] = "button"
        payload["sid"] = sid
        verify {
            mockEventServiceInternal.trackInternalCustomEventAsync("push:click", payload, null)
        }
    }
}