package com.emarsys.mobileengage.experimental;

import com.emarsys.mobileengage.api.experimental.MobileEngageFeature;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class MobileEngageFeatureTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test
    public void testGetName() {
        Assert.assertEquals("user_centric_inbox", MobileEngageFeature.USER_CENTRIC_INBOX.getName());
    }

}