package com.emarsys.mobileengage.notification.command

import android.content.Context
import android.content.Intent
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class OpenExternalUrlCommandTest {
    companion object {
        init {
            Mockito.mock(Context::class.java)
        }
    }

    @Rule
    var timeout: TestRule = timeoutRule

    @Test
    fun testRun_startsActivity_withCorrectIntent() {
        val mockContext: Context = mock()
        val intent = Intent()
        val command = OpenExternalUrlCommand(intent, mockContext)
        command.run()

        verify(mockContext).startActivity(intent)
    }
}