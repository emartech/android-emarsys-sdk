package com.emarsys.mobileengage.notification.command

import android.content.Context
import android.content.Intent
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OpenExternalUrlCommandTest {
    companion object {
        init {
            Mockito.mock(Context::class.java)
        }
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testRun_startsActivity_withCorrectIntent() {
        val mockContext: Context = mock()
        val intent = Intent()
        val command = OpenExternalUrlCommand(intent, mockContext)
        command.run()

        verify(mockContext).startActivity(intent)
    }
}