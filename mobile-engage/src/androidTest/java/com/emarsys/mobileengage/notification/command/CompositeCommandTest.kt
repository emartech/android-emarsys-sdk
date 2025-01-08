package com.emarsys.mobileengage.notification.command

import com.emarsys.testUtil.AnnotationSpec
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder

class CompositeCommandTest : AnnotationSpec() {
    @Test
    fun testRun_shouldInvokeAllCommands() {
        val command1: Runnable = mockk(relaxed = true)
        val command2: Runnable = mockk(relaxed = true)
        val command3: Runnable = mockk(relaxed = true)
        val compositeCommand: Runnable = CompositeCommand(listOf(command1, command2, command3))

        compositeCommand.run()

        verify { command1.run() }
        verify { command2.run() }
        verify { command3.run() }
    }

    @Test
    fun testRun_shouldInvokeAllCommands_inOrder_startingWithLaunchApplicationCommand() {
        val command1: CustomEventCommand = mockk(relaxed = true)
        val command2: LaunchApplicationCommand = mockk(relaxed = true)
        val command3: OpenExternalUrlCommand = mockk(relaxed = true)
        val compositeCommand: Runnable = CompositeCommand(listOf(command1, command2, command3))

        compositeCommand.run()

        verifyOrder {
            command2.run()
            command1.run()
            command3.run()
        }
    }
}