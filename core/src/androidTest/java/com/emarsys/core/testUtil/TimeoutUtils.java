package com.emarsys.core.testUtil;

import android.support.annotation.NonNull;
import android.support.test.rule.DisableOnAndroidDebug;

import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

public class TimeoutUtils {

    private TimeoutUtils() {
    }

    @NonNull
    public static TestRule getTimeoutRule() {
        return new DisableOnAndroidDebug(Timeout.seconds(30));
    }

}
