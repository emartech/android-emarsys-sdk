package com.emarsys.mobileengage.iam;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InAppInternalTest {

    InAppInternal inAppInternal;

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