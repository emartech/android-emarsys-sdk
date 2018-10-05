package com.emarsys.mobileengage.iam;

import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InAppInternalTest {

    InAppInternal inAppInternal;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        inAppInternal = new InAppInternal();
    }

    @Test
    public void testIsPaused_returnsFalse_byDefault() {
        assertFalse(inAppInternal.isPaused());
    }

    @Test
    public void testPause_setsIsPaused_toTrue() {
        inAppInternal.pause();

        assertTrue(inAppInternal.isPaused());
    }

    @Test
    public void testResume_setsIsPaused_toFalse() {
        inAppInternal.pause();
        inAppInternal.resume();

        assertFalse(inAppInternal.isPaused());
    }

}