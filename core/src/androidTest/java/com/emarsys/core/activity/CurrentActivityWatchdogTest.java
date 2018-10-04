package com.emarsys.core.activity;

import android.app.Activity;
import android.os.Bundle;

import com.emarsys.core.provider.Property;
import com.emarsys.core.provider.activity.CurrentActivityProvider;
import com.emarsys.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class CurrentActivityWatchdogTest {

    static {
        mock(Activity.class);
    }

    private CurrentActivityWatchdog watchdog;
    private Activity activity;
    private Activity nextActivity;
    private Property<Activity> activityProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        activityProvider = mock(Property.class);
        watchdog = new CurrentActivityWatchdog(activityProvider);
        activity = mock(Activity.class);
        nextActivity = mock(Activity.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_activityProvider_shouldNotBeNull() {
        new CurrentActivityWatchdog(null);
    }

    @Test
    public void testGetCurrentActivity_shouldStoreTheActivity_whenCallingOnResumed() {
        watchdog.onActivityResumed(activity);

        verify(activityProvider).set(activity);
    }

    @Test
    public void testGetCurrentActivity_newerActivity_shouldOverride_thePrevious() {
        watchdog.onActivityResumed(activity);
        watchdog.onActivityResumed(nextActivity);
        watchdog.onActivityPaused(activity);

        InOrder inOrder = Mockito.inOrder(activityProvider);
        inOrder.verify(activityProvider).set(activity);
        inOrder.verify(activityProvider).set(nextActivity);
    }

    @Test
    public void testGetCurrentActivity_shouldReturnNull_whenCurrentActivityPauses_andThereIsNoNextActivity() {
        activityProvider = new CurrentActivityProvider();
        watchdog = new CurrentActivityWatchdog(activityProvider);

        watchdog.onActivityResumed(activity);
        watchdog.onActivityPaused(activity);

        Assert.assertEquals(null, activityProvider.get());
    }

    @Test
    public void testGetCurrentActivity_otherLifecycleCallbacks_shouldBeIgnored() {
        Bundle bundle = new Bundle();

        watchdog.onActivityCreated(activity, bundle);
        watchdog.onActivityStarted(activity);
        watchdog.onActivityStopped(activity);
        watchdog.onActivitySaveInstanceState(activity, bundle);
        watchdog.onActivityDestroyed(activity);

        verifyZeroInteractions(activityProvider);
    }
}