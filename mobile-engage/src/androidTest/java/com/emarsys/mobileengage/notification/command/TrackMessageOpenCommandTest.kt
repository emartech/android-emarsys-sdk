package com.emarsys.mobileengage.notification.command

import android.content.Intent
import com.emarsys.mobileengage.MobileEngageInternal_V3_Old
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
        TrackMessageOpenCommand(mock(MobileEngageInternal_V3_Old::class.java), null)
    }
    
    @Test
    fun testRun_callsMobileEngageInternal() {
        val mobileEngageInternal = mock(MobileEngageInternal_V3_Old::class.java)
        val intent = mock(Intent::class.java)
        val command = TrackMessageOpenCommand(mobileEngageInternal, intent)

        command.run()

        verify(mobileEngageInternal).trackMessageOpen(intent, null)
    }

}