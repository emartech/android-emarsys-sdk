package com.emarsys.core.timestamp;

import com.emarsys.core.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class TimestampProviderTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test
    public void testProvideTimestamp_returnsTheCurrentTimestamp() {
        long before = System.currentTimeMillis();
        long actual = new TimestampProvider().provideTimestamp();
        long after = System.currentTimeMillis();

        Assert.assertTrue(before <= actual);
        Assert.assertTrue(actual <= after);
    }

}