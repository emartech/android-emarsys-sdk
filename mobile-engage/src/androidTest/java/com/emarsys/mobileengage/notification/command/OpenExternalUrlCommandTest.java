package com.emarsys.mobileengage.notification.command;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OpenExternalUrlCommandTest {

    static {
        mock(Context.class);
    }

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_intentMustNotBeNull() {
        new OpenExternalUrlCommand(null, InstrumentationRegistry.getTargetContext().getApplicationContext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_contextMustNotBeNull() {
        new OpenExternalUrlCommand(new Intent(), null);
    }

    @Test
    public void testRun_startsActivity_withCorrectIntent() {
        Context context = mock(Context.class);
        Intent intent = new Intent();

        OpenExternalUrlCommand command = new OpenExternalUrlCommand(intent, context);
        command.run();

        verify(context).startActivity(intent);
    }
}