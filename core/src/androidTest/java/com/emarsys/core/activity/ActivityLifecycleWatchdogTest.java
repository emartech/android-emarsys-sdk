package com.emarsys.core.activity;

import android.app.Activity;
import android.os.Bundle;

import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class ActivityLifecycleWatchdogTest {

    static {
        mock(Activity.class);
    }

    private ActivityLifecycleWatchdog watchdog;
    private ActivityLifecycleAction[] applicationStartActions;
    private ActivityLifecycleAction[] activityCreatedActions;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    private Activity activity1;
    private Activity activity2;
    private Activity activity3;

    @Before
    public void init() {
        activity1 = mock(Activity.class);
        activity2 = mock(Activity.class);
        activity3 = mock(Activity.class);
        applicationStartActions = initActions();
        activityCreatedActions = initActions();
        watchdog = new ActivityLifecycleWatchdog(applicationStartActions, activityCreatedActions);
    }

    @Test
    public void testConstructor_ApplicationStartActions_createEmptyArrayIfNull() {
        ActivityLifecycleWatchdog watchdog = new ActivityLifecycleWatchdog(null, activityCreatedActions);

        Assert.assertArrayEquals(new ActivityLifecycleAction[]{}, watchdog.getApplicationStartActions());
    }

    @Test
    public void testConstructor_activityCreatedActions_createEmptyArrayIfNull() {
        ActivityLifecycleWatchdog watchdog = new ActivityLifecycleWatchdog(applicationStartActions, null);

        Assert.assertArrayEquals(new ActivityLifecycleAction[]{}, watchdog.getActivityCreatedActions());
    }

    @Test
    public void testConstructor_fieldsInitialized_withConstructorArguments() {
        ActivityLifecycleWatchdog watchdog = new ActivityLifecycleWatchdog(applicationStartActions, activityCreatedActions);

        Assert.assertArrayEquals(applicationStartActions, watchdog.getApplicationStartActions());
        Assert.assertArrayEquals(activityCreatedActions, watchdog.getActivityCreatedActions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ApplicationStartedActions_mustNotContain_nullElements() {
        ActivityLifecycleAction[] actions = {
                mock(ActivityLifecycleAction.class),
                null,
                mock(ActivityLifecycleAction.class)};
        new ActivityLifecycleWatchdog(actions, activityCreatedActions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ActivityCreatedActions_mustNotContain_nullElements() {
        ActivityLifecycleAction[] actions = {
                mock(ActivityLifecycleAction.class),
                null,
                mock(ActivityLifecycleAction.class)};
        new ActivityLifecycleWatchdog(applicationStartActions, actions);
    }

    @Test
    public void testApplicationStart_onResume_shouldInvokeActions() {
        watchdog.onActivityResumed(activity1);

        verifyExecuteCalled(applicationStartActions, 1);
    }

    @Test
    public void testApplicationStart_testOtherCallbacks_shouldNotInvokeActions() {
        watchdog.onActivityCreated(activity1, new Bundle());
        watchdog.onActivityStarted(activity1);
        watchdog.onActivityPaused(activity1);
        watchdog.onActivityStopped(activity1);
        watchdog.onActivitySaveInstanceState(activity1, new Bundle());
        watchdog.onActivityDestroyed(activity1);

        verifyExecuteCalled(applicationStartActions, 0);
    }

    @Test
    public void testApplicationStart_onResume_actionsShouldBeInvoked_onlyOnce_inTheSameSession() {
        watchdog.onActivityCreated(activity1, new Bundle());
        watchdog.onActivityStarted(activity1);
        watchdog.onActivityResumed(activity1);
        watchdog.onActivityPaused(activity1);
        watchdog.onActivityCreated(activity2, new Bundle());
        watchdog.onActivityStarted(activity2);
        watchdog.onActivityResumed(activity2);
        watchdog.onActivityStopped(activity1);

        watchdog.onActivityPaused(activity2);
        watchdog.onActivityCreated(activity3, new Bundle());
        watchdog.onActivityStarted(activity3);
        watchdog.onActivityResumed(activity3);
        watchdog.onActivityStopped(activity2);

        verifyExecuteCalled(applicationStartActions, 1);
    }

    @Test
    public void testApplicationStart_onResume_actionsShouldBeInvoked_multipleTimes_forEachSession() {
        watchdog.onActivityCreated(activity1, new Bundle());
        watchdog.onActivityStarted(activity1);
        watchdog.onActivityResumed(activity1);
        watchdog.onActivityPaused(activity1);
        watchdog.onActivityStopped(activity1);

        verifyExecuteCalled(applicationStartActions, 1);

        watchdog.onActivityCreated(activity2, new Bundle());
        watchdog.onActivityStarted(activity2);
        watchdog.onActivityResumed(activity2);
        watchdog.onActivityPaused(activity2);
        watchdog.onActivityStopped(activity2);

        verifyExecuteCalled(applicationStartActions, 2);
    }

    @Test
    public void testApplicationStart_onResume_actionsShouldBeInvoked_multipleTimes_forEachSession_withActivityTransitions() {
        watchdog.onActivityCreated(activity1, new Bundle());
        watchdog.onActivityStarted(activity1);
        watchdog.onActivityResumed(activity1);
        watchdog.onActivityPaused(activity1);
        watchdog.onActivityCreated(activity2, new Bundle());
        watchdog.onActivityStarted(activity2);
        watchdog.onActivityResumed(activity2);
        watchdog.onActivityStopped(activity1);
        watchdog.onActivityPaused(activity2);
        watchdog.onActivityStopped(activity2);

        verifyExecuteCalled(applicationStartActions, 1);

        watchdog.onActivityCreated(activity3, new Bundle());
        watchdog.onActivityStarted(activity3);
        watchdog.onActivityResumed(activity3);

        verifyExecuteCalled(applicationStartActions, 2);
    }

    @Test
    public void testActivityCreated_onActivityCreated_shouldInvokeActions() {
        watchdog.onActivityCreated(activity1, new Bundle());
        verifyExecuteCalled(activityCreatedActions, 1);

        watchdog.onActivityCreated(activity2, new Bundle());
        verifyExecuteCalled(activityCreatedActions, 2);

        watchdog.onActivityCreated(activity3, new Bundle());
        verifyExecuteCalled(activityCreatedActions, 3);
    }

    @Test
    public void testActivityCreated_testOtherCallbacks_shouldNotInvokeActions() {
        watchdog.onActivityStarted(activity1);
        watchdog.onActivityResumed(activity1);
        watchdog.onActivityPaused(activity1);
        watchdog.onActivityStopped(activity1);
        watchdog.onActivitySaveInstanceState(activity1, new Bundle());
        watchdog.onActivityDestroyed(activity1);

        verifyExecuteCalled(activityCreatedActions, 0);
    }

    @Test
    public void testTriggerOnActivity_shouldTriggerTheActionOnNewActivity_whenThereIsNoCurrentActivity() {
        ActivityLifecycleAction action = mock(ActivityLifecycleAction.class);

        watchdog.addTriggerOnActivityAction(action);
        watchdog.onActivityCreated(activity1, new Bundle());
        watchdog.onActivityStarted(activity1);
        watchdog.onActivityResumed(activity1);
        verify(action).execute(activity1);
    }

    @Test
    public void testTriggerOnActivity_shouldTriggerTheActionOnTheCurrentActivity() {
        ActivityLifecycleAction action = mock(ActivityLifecycleAction.class);

        watchdog.onActivityCreated(activity1, new Bundle());
        watchdog.onActivityStarted(activity1);
        watchdog.onActivityResumed(activity1);
        verifyZeroInteractions(action);
        watchdog.addTriggerOnActivityAction(action);
        verify(action).execute(activity1);
    }

    @Test
    public void testTriggerOnActivity_shouldRemoveTriggeredActions() {
        ActivityLifecycleAction action = mock(ActivityLifecycleAction.class);

        watchdog.onActivityCreated(activity1, new Bundle());
        watchdog.onActivityStarted(activity1);
        watchdog.onActivityResumed(activity1);
        verifyZeroInteractions(action);
        watchdog.addTriggerOnActivityAction(action);
        verify(action).execute(activity1);
        assertEquals(0, watchdog.getTriggerOnActivityActions().size());
    }

    private ActivityLifecycleAction[] initActions() {
        int size = 5;
        ActivityLifecycleAction[] activityLifecycleActions = new ActivityLifecycleAction[size];
        for (int i = 0; i < 5; ++i) {
            activityLifecycleActions[i] = mock(ActivityLifecycleAction.class);
        }
        return activityLifecycleActions;
    }

    private void verifyExecuteCalled(ActivityLifecycleAction[] actions, int times) {
        for (ActivityLifecycleAction action : actions) {
            verify(action, times(times)).execute(any(Activity.class));
        }
    }
}