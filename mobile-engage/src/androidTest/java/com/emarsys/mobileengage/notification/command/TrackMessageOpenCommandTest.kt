package com.emarsys.mobileengage.notification.command

import com.emarsys.mobileengage.push.PushInternal

import com.emarsys.testUtil.AnnotationSpec

import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class TrackMessageOpenCommandTest : AnnotationSpec() {


    @Test
    fun testRun_callsMobileEngageInternal() {
        val pushInternal = mock(PushInternal::class.java)
        val sid = "test sid"
        val command = TrackMessageOpenCommand(pushInternal, sid)

        command.run()

        verify(pushInternal).trackMessageOpen(sid, null)
    }

}