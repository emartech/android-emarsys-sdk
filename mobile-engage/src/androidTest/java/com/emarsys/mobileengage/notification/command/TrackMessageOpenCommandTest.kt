package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.push.PushInternal
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TrackMessageOpenCommandTest {


    @Test
    fun testRun_callsMobileEngageInternal() {
        val mockPushInternal = mockk<PushInternal>(relaxed = true)
        val sid = "test sid"
        val command = TrackMessageOpenCommand(mockPushInternal, sid)

        command.run()

        verify { mockPushInternal.trackMessageOpen(sid, null) }
    }

}