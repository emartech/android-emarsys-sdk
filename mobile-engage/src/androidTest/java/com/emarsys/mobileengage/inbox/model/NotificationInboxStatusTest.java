package com.emarsys.mobileengage.inbox.model;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class NotificationInboxStatusTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testConstructor_notificationsShouldNotBeNull() {
        Assert.assertNotNull(new NotificationInboxStatus(null, 0).getNotifications());
    }

}
