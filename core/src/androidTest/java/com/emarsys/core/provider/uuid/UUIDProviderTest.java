package com.emarsys.core.provider.uuid;

import static org.junit.Assert.assertNotNull;

import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;


public class UUIDProviderTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();


    @Test
    public void testProvideId_returnsNotNullId() {
        UUIDProvider provider = new UUIDProvider();
        assertNotNull(provider.provideId());
    }

}