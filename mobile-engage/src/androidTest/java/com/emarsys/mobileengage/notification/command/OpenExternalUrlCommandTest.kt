package com.emarsys.mobileengage.notification.command

import android.content.Context
import android.content.Intent
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class OpenExternalUrlCommandTest {
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
    }

    @Test
    fun testRun_startsActivity_withCorrectIntent() {
        val intent = Intent()
        val command = OpenExternalUrlCommand(intent, mockContext)
        command.run()

        verify { mockContext.startActivity(intent) }
    }
}