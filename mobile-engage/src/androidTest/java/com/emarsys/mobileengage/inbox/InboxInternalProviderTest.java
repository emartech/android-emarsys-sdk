package com.emarsys.mobileengage.inbox;

import com.emarsys.core.request.RequestManager;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.request.RequestModelFactory;
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
                mock(RequestContext.class),
                mock(RequestModelFactory.class)
        );
        Assert.assertEquals(InboxInternal_V1.class, inboxInternal.getClass());
    }
}