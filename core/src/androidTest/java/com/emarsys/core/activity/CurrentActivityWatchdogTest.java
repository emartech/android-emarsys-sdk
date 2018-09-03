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
import static org.mockito.Mockito.verifyZeroInteractions;

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
        CurrentActivityWatchdog.reset();

        watchdog = new CurrentActivityWatchdog();
        application = mock(Application.class);
        activity = mock(Activity.class);
        nextActivity = mock(Activity.class);
    }

    @Test
    public void testRegisterApplication_shouldRegisterForLifecycleCallbacks() {
        CurrentActivityWatchdog.registerApplication(application);

        verify(application).registerActivityLifecycleCallbacks(any(CurrentActivityWatchdog.class));
    }

    @Test
    public void testRegisterApplication_shouldDoNothing_whenAlreadyRegistered() {
        CurrentActivityWatchdog.registerApplication(application);

        Application secondApplication = mock(Application.class);
        CurrentActivityWatchdog.registerApplication(secondApplication);
        verifyZeroInteractions(secondApplication);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetCurrentActivity_shouldFail_ifWatchdogWasNotRegisteredBeforehand() {
        CurrentActivityWatchdog.getCurrentActivity();
    }

    @Test
    public void testGetCurrentActivity_shouldStoreTheActivity_whenCallingOnResumed() {
        CurrentActivityWatchdog.registerApplication(application);
        watchdog.onActivityResumed(activity);

        assertEquals(activity, CurrentActivityWatchdog.getCurrentActivity());
    }

    @Test
    public void testGetCurrentActivity_newerActivity_shouldOverride_thePrevious() {
        CurrentActivityWatchdog.registerApplication(application);

        watchdog.onActivityResumed(activity);
        watchdog.onActivityResumed(nextActivity);
        watchdog.onActivityPaused(activity);

        assertEquals(nextActivity, CurrentActivityWatchdog.getCurrentActivity());
    }

    @Test
    public void testGetCurrentActivity_shouldReturnNull_whenCurrentActivityPauses_andThereIsNoNextActivity() {
        CurrentActivityWatchdog.registerApplication(application);

        watchdog.onActivityResumed(activity);
        watchdog.onActivityPaused(activity);

        assertNull(CurrentActivityWatchdog.getCurrentActivity());
    }

    @Test
    public void testGetCurrentActivity_otherLifecycleCallbacks_shouldBeIgnored() {
        CurrentActivityWatchdog.registerApplication(application);
        Bundle bundle = new Bundle();

        watchdog.onActivityCreated(activity, bundle);
        watchdog.onActivityStarted(activity);
        watchdog.onActivityStopped(activity);
        watchdog.onActivitySaveInstanceState(activity, bundle);
        watchdog.onActivityDestroyed(activity);

        assertNull(CurrentActivityWatchdog.getCurrentActivity());
    }
}