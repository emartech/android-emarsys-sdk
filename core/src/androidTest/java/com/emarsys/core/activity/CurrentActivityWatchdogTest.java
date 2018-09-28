package com.emarsys.core.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CurrentActivityWatchdogTest {

    static {
        mock(Application.class);
        mock(Activity.class);
    }

    private CurrentActivityWatchdog watchdog;
    private Application application;
    private Activity activity;
    private Activity nextActivity;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        application = mock(Application.class);
        watchdog = new CurrentActivityWatchdog(application);
        activity = mock(Activity.class);
        nextActivity = mock(Activity.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_application_shouldNotBeNull() {
        new CurrentActivityWatchdog(null);
    }

    @Test
    public void testRegisterApplication_shouldRegisterForLifecycleCallbacks() {
        application = mock(Application.class);
        new CurrentActivityWatchdog(application);

        verify(application).registerActivityLifecycleCallbacks(any(CurrentActivityWatchdog.class));
    }

    @Test
    public void testGetCurrentActivity_shouldStoreTheActivity_whenCallingOnResumed() {
        watchdog.onActivityResumed(activity);

        assertEquals(activity, watchdog.getCurrentActivity());
    }

    @Test
    public void testGetCurrentActivity_newerActivity_shouldOverride_thePrevious() {
        watchdog.onActivityResumed(activity);
        watchdog.onActivityResumed(nextActivity);
        watchdog.onActivityPaused(activity);

        assertEquals(nextActivity, watchdog.getCurrentActivity());
    }

    @Test
    public void testGetCurrentActivity_shouldReturnNull_whenCurrentActivityPauses_andThereIsNoNextActivity() {
        watchdog.onActivityResumed(activity);
        watchdog.onActivityPaused(activity);

        assertNull(watchdog.getCurrentActivity());
    }

    @Test
    public void testGetCurrentActivity_otherLifecycleCallbacks_shouldBeIgnored() {
        Bundle bundle = new Bundle();

        watchdog.onActivityCreated(activity, bundle);
        watchdog.onActivityStarted(activity);
        watchdog.onActivityStopped(activity);
        watchdog.onActivitySaveInstanceState(activity, bundle);
        watchdog.onActivityDestroyed(activity);

        assertNull(watchdog.getCurrentActivity());
    }
}