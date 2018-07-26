package com.emarsys.mobileengage.notification.command;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CompositeCommandTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_commands_mustNotBeNull() {
        new CompositeCommand(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_commands_mustNotContainNullElements() {
        List<Runnable> commands = Arrays.asList(
                mock(Runnable.class),
                mock(Runnable.class),
                null
        );
        new CompositeCommand(commands);
    }

    @Test
    public void testRun_shouldInvokeAllCommands() {
        Runnable command1 = mock(Runnable.class);
        Runnable command2 = mock(Runnable.class);
        Runnable command3 = mock(Runnable.class);

        Runnable compositeCommand = new CompositeCommand(Arrays.asList(command1, command2, command3));

        compositeCommand.run();

        verify(command1).run();
        verify(command2).run();
        verify(command3).run();
    }

    @Test
    public void testRun_shouldInvokeAllCommands_inOrder() {
        Runnable command1 = mock(Runnable.class);
        Runnable command2 = mock(Runnable.class);
        Runnable command3 = mock(Runnable.class);

        Runnable compositeCommand = new CompositeCommand(Arrays.asList(command1, command2, command3));

        compositeCommand.run();

        InOrder inOrder = Mockito.inOrder(command1, command2, command3);
        inOrder.verify(command1).run();
        inOrder.verify(command2).run();
        inOrder.verify(command3).run();
    }

}