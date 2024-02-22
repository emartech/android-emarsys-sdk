package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.push.PushInternal

import org.junit.jupiter.api.Test

import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class TrackMessageOpenCommandTest {


    @Test
    fun testRun_callsMobileEngageInternal() {
        val pushInternal = mock(PushInternal::class.java)
        val sid = "test sid"
        val command = TrackMessageOpenCommand(pushInternal, sid)

        command.run()

        verify(pushInternal).trackMessageOpen(sid, null)
    }

}