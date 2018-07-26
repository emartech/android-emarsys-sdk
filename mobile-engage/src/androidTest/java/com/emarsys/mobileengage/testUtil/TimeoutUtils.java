package com.emarsys.mobileengage.testUtil;

import android.support.test.rule.DisableOnAndroidDebug;

import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

public class TimeoutUtils {

    private TimeoutUtils() {
    }

    public static TestRule getTimeoutRule() {
        return new DisableOnAndroidDebug(Timeout.seconds(30));
    }

}
