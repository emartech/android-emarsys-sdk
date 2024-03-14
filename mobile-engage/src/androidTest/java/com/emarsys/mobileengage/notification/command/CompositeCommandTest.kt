package com.emarsys.mobileengage.notification.command


import com.emarsys.testUtil.AnnotationSpec

import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class CompositeCommandTest : AnnotationSpec() {


    @Test
    fun testRun_shouldInvokeAllCommands() {
        val command1: Runnable = mock()
        val command2: Runnable = mock()
        val command3: Runnable = mock()
        val compositeCommand: Runnable = CompositeCommand(listOf(command1, command2, command3))

        compositeCommand.run()

        verify(command1).run()
        verify(command2).run()
        verify(command3).run()
    }

    @Test
    fun testRun_shouldInvokeAllCommands_inOrder_startingWithLaunchApplicationCommand() {
        val command1: CustomEventCommand = mock()
        val command2: LaunchApplicationCommand = mock()
        val command3: OpenExternalUrlCommand = mock()
        val compositeCommand: Runnable = CompositeCommand(listOf(command1, command2, command3))

        compositeCommand.run()

        val inOrder = inOrder(command2, command1, command3)

        inOrder.verify(command2).run()
        inOrder.verify(command1).run()
        inOrder.verify(command3).run()
    }
}