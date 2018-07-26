package com.emarsys.core.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import com.emarsys.core.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ActivityLifecycleWatchdog implements Application.ActivityLifecycleCallbacks {

    private Activity currentActivity;
    private final ActivityLifecycleAction[] applicationStartActions;
    private final ActivityLifecycleAction[] activityCreatedActions;
    private List<ActivityLifecycleAction> triggerOnActivityActions;

    public ActivityLifecycleWatchdog(ActivityLifecycleAction[] applicationStartActions, ActivityLifecycleAction[] activityCreatedActions) {
        this.applicationStartActions = initializeActionsIfNull(applicationStartActions);
        this.activityCreatedActions = initializeActionsIfNull(activityCreatedActions);
        Assert.elementsNotNull(this.applicationStartActions, "ApplicationStartActions must not contain null elements!");
        Assert.elementsNotNull(this.activityCreatedActions, "ActivityCreatedActions must not contain null elements!");
        triggerOnActivityActions = new ArrayList<>();
    }

    public void addTriggerOnActivityAction(ActivityLifecycleAction triggerOnActivityAction) {
        triggerOnActivityActions.add(triggerOnActivityAction);
        if (currentActivity != null) {
            triggerOnActivity();
        }
    }

    public ActivityLifecycleAction[] getApplicationStartActions() {
        return applicationStartActions;
    }

    public ActivityLifecycleAction[] getActivityCreatedActions() {
        return activityCreatedActions;
    }

    public List<ActivityLifecycleAction> getTriggerOnActivityActions() {
        return triggerOnActivityActions;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        for (ActivityLifecycleAction action : activityCreatedActions) {
            action.execute(activity);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (currentActivity == null) {
            for (ActivityLifecycleAction action : applicationStartActions) {
                action.execute(activity);
            }
        }
        currentActivity = activity;
        triggerOnActivity();
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (currentActivity == activity) {
            currentActivity = null;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    private void triggerOnActivity() {
        for (int i = triggerOnActivityActions.size() - 1; i >= 0; i--) {
            ActivityLifecycleAction action = triggerOnActivityActions.remove(i);
            action.execute(currentActivity);
        }
    }

    private ActivityLifecycleAction[] initializeActionsIfNull(ActivityLifecycleAction[] actions) {
        return actions != null ? actions : new ActivityLifecycleAction[]{};
    }

}
