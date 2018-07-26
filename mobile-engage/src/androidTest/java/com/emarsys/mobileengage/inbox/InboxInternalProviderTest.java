package com.emarsys.mobileengage.inbox;

import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RestClient;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

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
                false,
                mock(RequestManager.class),
                mock(RestClient.class),
                mock(RequestContext.class)
        );
        Assert.assertEquals(InboxInternal_V1.class, inboxInternal.getClass());
    }

    @Test
    public void testProvide_returnsInternal_V2_withExperimentalTrue() {
        InboxInternal inboxInternal = provider.provideInboxInternal(
                true,
                mock(RequestManager.class),
                mock(RestClient.class),
                mock(RequestContext.class)
        );
        Assert.assertEquals(InboxInternal_V2.class, inboxInternal.getClass());
    }
}