package com.emarsys.mobileengage.iam;

import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DoNotDisturbProviderTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @After
    public void tearDown() {
        MobileEngage.InApp.setPaused(false);
    }

    @Test
    public void testIsPaused_returnsTrue_ifInAppIsSetToBePaused() {
        MobileEngage.InApp.setPaused(true);
        DoNotDisturbProvider provider = new DoNotDisturbProvider();

        assertTrue(provider.isPaused());
    }

    @Test
    public void testIsPaused_returnsFalse_ifInAppIsSetToBeResumed() {
        MobileEngage.InApp.setPaused(false);
        DoNotDisturbProvider provider = new DoNotDisturbProvider();

        assertFalse(provider.isPaused());
    }
}