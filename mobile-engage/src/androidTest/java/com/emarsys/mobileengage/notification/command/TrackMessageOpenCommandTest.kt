package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class TrackMessageOpenCommandTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testRun_callsMobileEngageInternal() {
        val pushInternal = mock(PushInternal::class.java)
        val sid = "test sid"
        val command = TrackMessageOpenCommand(pushInternal, sid)

        command.run()

        verify(pushInternal).trackMessageOpen(sid, null)
    }

}