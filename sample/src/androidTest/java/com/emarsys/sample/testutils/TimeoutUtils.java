package com.emarsys.sample.testutils;


import androidx.test.rule.DisableOnAndroidDebug;

import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

public class TimeoutUtils {
    public static DisableOnAndroidDebug getTimeoutRule() {
        return new DisableOnAndroidDebug(new Timeout(30, TimeUnit.SECONDS));
    }
}
