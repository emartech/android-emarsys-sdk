package com.emarsys;

import com.emarsys.core.RunnerProxy;
import com.emarsys.inapp.InAppProxy;
import com.emarsys.mobileengage.api.EventHandler;
import com.emarsys.mobileengage.iam.InAppInternal;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InAppProxyTest {
    private InAppProxy inAppProxy;

    private InAppInternal mockInAppInternal;

    private RunnerProxy runnerProxy;


    @Before
    public void setUp() {
        runnerProxy = new RunnerProxy();
        mockInAppInternal = mock(InAppInternal.class);

        inAppProxy = new InAppProxy(runnerProxy, mockInAppInternal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_runnerProxy_mustNotBeNull() {
        new InAppProxy(null, mockInAppInternal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_inAppInternal_mustNotBeNull() {
        new InAppProxy(runnerProxy, null);
    }

    @Test
    public void testInApp_pause_delegatesToInternal() {
        inAppProxy.pause();

        verify(mockInAppInternal).pause();
    }

    @Test
    public void testInApp_resume_delegatesToInternal() {
        inAppProxy.resume();

        verify(mockInAppInternal).resume();
    }

    @Test
    public void testInApp_isPaused_delegatesToInternal() {
        when(mockInAppInternal.isPaused()).thenReturn(true);

        boolean result = inAppProxy.isPaused();

        verify(mockInAppInternal).isPaused();
        assertTrue(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInApp_setEventHandler_eventHandler_mustNotBeNull() {
        inAppProxy.setEventHandler(null);
    }

    @Test
    public void testInApp_setEventHandler_delegatesToInternal() {
        EventHandler eventHandler = mock(EventHandler.class);

        inAppProxy.setEventHandler(eventHandler);

        verify(mockInAppInternal).setEventHandler(eventHandler);
    }

}