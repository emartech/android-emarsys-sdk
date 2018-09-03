package com.emarsys.core.provider.uuid;

import com.emarsys.test.util.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertNotNull;


public class UUIDProviderTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();


    @Test
    public void testProvideId_returnsNotNullId() {
        UUIDProvider provider = new UUIDProvider();
        assertNotNull(provider.provideId());
    }

}