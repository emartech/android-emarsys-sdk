package com.emarsys.mobileengage.notification.command

import android.content.Intent
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

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_mobileEngageInternal_mustNotBeNull() {
        TrackMessageOpenCommand(null, Intent())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_intent_mustNotBeNull() {
        TrackMessageOpenCommand(mock(PushInternal::class.java), null)
    }

    @Test
    fun testRun_callsMobileEngageInternal() {
        val pushInternal = mock(PushInternal::class.java)
        val intent = mock(Intent::class.java)
        val command = TrackMessageOpenCommand(pushInternal, intent)

        command.run()

        verify(pushInternal).trackMessageOpen(intent, null)
    }

}