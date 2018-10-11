package com.emarsys.core;

import com.emarsys.core.response.AbstractResponseHandler;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class DefaultCompletionHandlerTest {

    private AbstractResponseHandler abstractResponseHandler1;
    private AbstractResponseHandler abstractResponseHandler2;
    private List<AbstractResponseHandler> handlers;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        abstractResponseHandler1 = mock(AbstractResponseHandler.class);
        abstractResponseHandler2 = mock(AbstractResponseHandler.class);
        handlers = Arrays.asList(abstractResponseHandler1, abstractResponseHandler2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_handlersShouldNotBeNull() {
        new DefaultCompletionHandler(null);
    }

    @Test
    public void testAddResponseHandlers() {
        DefaultCompletionHandler coreCompletionHandler = new DefaultCompletionHandler(new ArrayList<AbstractResponseHandler>());
        coreCompletionHandler.addResponseHandlers(handlers);

        assertEquals(handlers, coreCompletionHandler.responseHandlers);
    }

}