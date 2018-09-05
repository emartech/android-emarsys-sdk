package com.emarsys.mobileengage.experimental;

import com.emarsys.core.experimental.ExperimentalFeatures;
import com.emarsys.mobileengage.api.experimental.MobileEngageFeature;
import com.emarsys.testUtil.ReflectionTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MobileEngageExperimentalTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setUp() {
        ReflectionTestUtils.invokeStaticMethod(ExperimentalFeatures.class, "reset");
    }

    @After
    public void tearDown() {
        ReflectionTestUtils.invokeStaticMethod(ExperimentalFeatures.class, "reset");
    }

    @Test
    public void testIsV3Enabled_returnsFalse_ifNeitherInAppNOrInbox_isTurnedOn() {
        assertFalse(MobileEngageExperimentalFeatures.isV3Enabled());
    }

    @Test
    public void testIsV3Enabled_returnsTrue_ifInApp_isTurnedOn() {
        ExperimentalFeatures.enableFeature(MobileEngageFeature.IN_APP_MESSAGING);
        assertTrue(MobileEngageExperimentalFeatures.isV3Enabled());
    }

    @Test
    public void testIsV3Enabled_returnsTrue_ifInboxV2_isTurnedOn() {
        ExperimentalFeatures.enableFeature(MobileEngageFeature.USER_CENTRIC_INBOX);
        assertTrue(MobileEngageExperimentalFeatures.isV3Enabled());
    }

    @Test
    public void testIsV3Enabled_returnsTrue_ifBoth_inAppAndInboxV2_areTurnedOn() {
        ExperimentalFeatures.enableFeature(MobileEngageFeature.IN_APP_MESSAGING);
        ExperimentalFeatures.enableFeature(MobileEngageFeature.USER_CENTRIC_INBOX);
        assertTrue(MobileEngageExperimentalFeatures.isV3Enabled());
    }
}