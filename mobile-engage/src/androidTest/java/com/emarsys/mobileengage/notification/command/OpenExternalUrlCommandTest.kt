package com.emarsys.mobileengage.notification.command

import android.content.Context
import android.content.Intent

import org.junit.jupiter.api.Test

import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OpenExternalUrlCommandTest {
    companion object {
        init {
            Mockito.mock(Context::class.java)
        }
    }


    @Test
    fun testRun_startsActivity_withCorrectIntent() {
        val mockContext: Context = mock()
        val intent = Intent()
        val command = OpenExternalUrlCommand(intent, mockContext)
        command.run()

        verify(mockContext).startActivity(intent)
    }
}