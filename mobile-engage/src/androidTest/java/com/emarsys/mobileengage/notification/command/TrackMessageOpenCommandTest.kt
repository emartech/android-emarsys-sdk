package com.emarsys.mobileengage.notification.command

import android.content.Intent
import com.emarsys.mobileengage.MobileEngageInternal
import org.junit.Test

import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class TrackMessageOpenCommandTest {

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_mobileEngageInternal_mustNotBeNull() {
        TrackMessageOpenCommand(null, Intent())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_intent_mustNotBeNull() {
        TrackMessageOpenCommand(mock(MobileEngageInternal::class.java), null)
    }
    
    @Test
    fun testRun_callsMobileEngageInternal() {
        val mobileEngageInternal = mock(MobileEngageInternal::class.java)
        val intent = mock(Intent::class.java)
        val command = TrackMessageOpenCommand(mobileEngageInternal, intent)

        command.run()

        verify(mobileEngageInternal).trackMessageOpen(intent, null)
    }

}