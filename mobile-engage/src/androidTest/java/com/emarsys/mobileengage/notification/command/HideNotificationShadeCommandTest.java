package com.emarsys.mobileengage.notification.command;

import android.content.Context;
import android.content.Intent;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class HideNotificationShadeCommandTest {

    static {
        mock(Context.class);
    }

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_contextMustNotBeNull() {
        new HideNotificationShadeCommand(null);
    }

    @Test
    public void testRun_triggersNotificationShadeCloseBroadcast() {
        Context mockContext = mock(Context.class);

        new HideNotificationShadeCommand(mockContext).run();

        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(mockContext).sendBroadcast(captor.capture());
        Intent intent = captor.getValue();

        Assert.assertEquals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS, intent.getAction());
    }

}