package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.event.EventServiceInternal
import io.kotest.assertions.throwables.shouldThrow
import org.junit.Test
import org.mockito.Mockito

class TrackActionClickCommandTest  {
    @Test
    fun testConstructor_eventServiceInternal_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            TrackActionClickCommand(null, "", "")
        }
    }

    @Test
    fun testConstructor_buttonId_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            TrackActionClickCommand(Mockito.mock(EventServiceInternal::
class.java), null, "")
        }
    }

    @Test
    fun testConstructor_sid_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            TrackActionClickCommand(Mockito.mock(EventServiceInternal::
class.java), "", null)
        }
    }

    @Test
    fun testRun_sendsInternalCustomEvent() {
        val internalMock = Mockito.mock(
            EventServiceInternal::
class.java
        )
        val buttonId = "buttonId"
        val sid = "sid1234"
        TrackActionClickCommand(internalMock, buttonId, sid).run()
        val payload: MutableMap<String, String> = HashMap()
        payload["button_id"] = buttonId
        payload["origin"] = "button"
        payload["sid"] = sid
        Mockito.verify(internalMock).trackInternalCustomEventAsync("push:click", payload, null)
    }
}