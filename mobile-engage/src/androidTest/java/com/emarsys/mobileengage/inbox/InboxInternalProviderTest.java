package com.emarsys.mobileengage.inbox;

import com.emarsys.core.request.RequestManager;
import com.emarsys.mobileengage.MobileEngageRequestContext;
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory;
import com.emarsys.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;

public class InboxInternalProviderTest {

    private InboxInternalProvider provider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        provider = new InboxInternalProvider();
    }

    @Test
    public void testProvide_returnsInternal_V1_withExperimentalFalse() {
        InboxInternal inboxInternal = provider.provideInboxInternal(
                mock(RequestManager.class),
                mock(MobileEngageRequestContext.class),
                mock(MobileEngageRequestModelFactory.class)
        );
        Assert.assertEquals(DefaultInboxInternal.class, inboxInternal.getClass());
    }
}