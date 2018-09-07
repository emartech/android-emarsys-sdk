package com.emarsys.core.di;

import android.os.Handler;

import com.emarsys.core.activity.ActivityLifecycleWatchdog;

public interface DependencyContainer {

    Handler getCoreSdkHandler();

    ActivityLifecycleWatchdog getActivityLifecycleWatchdog();

}
