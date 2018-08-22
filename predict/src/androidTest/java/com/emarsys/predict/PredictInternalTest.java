package com.emarsys.predict;

import com.emarsys.core.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class PredictInternalTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testSetTag_tagShouldNotBeNull() {
        new PredictInternal().setTag(null);
    }
}