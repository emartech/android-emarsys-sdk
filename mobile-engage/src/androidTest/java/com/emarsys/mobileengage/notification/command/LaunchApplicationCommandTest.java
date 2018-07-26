package com.emarsys.mobileengage.notification.command;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LaunchApplicationCommandTest {

    static {
        mock(Activity.class);
        mock(PackageManager.class);
    }

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_intentMustNotBeNull() {
        new LaunchApplicationCommand(null, InstrumentationRegistry.getTargetContext().getApplicationContext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_contextMustNotBeNull() {
        new LaunchApplicationCommand(new Intent(), null);
    }

    @Test
    public void testRun_startsActivity_withCorrectIntent() {
        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);

        Intent launchIntentForPackage = new Intent();
        PackageManager pm = mock(PackageManager.class);
        when(pm.getLaunchIntentForPackage(anyString())).thenReturn(launchIntentForPackage);
        Activity mockActivity = mock(Activity.class);
        when(mockActivity.getPackageManager()).thenReturn(pm);
        when(mockActivity.getPackageName()).thenReturn("packageName");

        Bundle extras = new Bundle();
        extras.putLong("key1", 800);
        extras.putString("key2", "value");
        Intent remoteIntent = new Intent();
        remoteIntent.putExtras(extras);

        Runnable command = new LaunchApplicationCommand(remoteIntent, mockActivity);

        command.run();

        verify(mockActivity).startActivity(captor.capture());

        Bundle expectedBundle = launchIntentForPackage.getExtras();
        Bundle resultBundle = captor.getValue().getExtras();
        Assert.assertEquals(expectedBundle.keySet(), resultBundle.keySet());

        for (String key : expectedBundle.keySet()) {
            Assert.assertEquals(expectedBundle.get(key), resultBundle.get(key));
        }
    }

}