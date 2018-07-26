package com.emarsys.core.request;

import com.emarsys.core.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertNotNull;


public class RequestIdProviderTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();


    @Test
    public void testProvideId_returnsNotNullId() {
        RequestIdProvider provider = new RequestIdProvider();
        assertNotNull(provider.provideId());
    }

}