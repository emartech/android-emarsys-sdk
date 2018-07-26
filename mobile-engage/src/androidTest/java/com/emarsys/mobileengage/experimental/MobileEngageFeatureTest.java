package com.emarsys.mobileengage.experimental;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class MobileEngageFeatureTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test
    public void testGetName() {
        Assert.assertEquals("in_app_messaging", MobileEngageFeature.IN_APP_MESSAGING.getName());
    }

}