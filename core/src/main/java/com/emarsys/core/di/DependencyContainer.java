package com.emarsys.core.di;

import android.os.Handler;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.activity.ActivityLifecycleWatchdog;
import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.database.CoreSQLiteDatabase;

public interface DependencyContainer {

    Handler getCoreSdkHandler();

    ActivityLifecycleWatchdog getActivityLifecycleWatchdog();

    CurrentActivityWatchdog getCurrentActivityWatchdog();

    CoreSQLiteDatabase getCoreSQLiteDatabase();

    DeviceInfo getDeviceInfo();

}
