package com.emarsys.core;

import com.emarsys.core.DefaultCompletionHandler;
import com.emarsys.core.StatusListener;
import com.emarsys.core.response.AbstractResponseHandler;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        new DefaultCompletionHandler(null, mock(StatusListener.class));
    }

    @Test
    public void testOnSuccess_shouldCallProcessResponseOnTheHandlers() throws Exception {
        DefaultCompletionHandler coreCompletionHandler = new DefaultCompletionHandler(handlers, null);
        ResponseModel responseModel = mock(ResponseModel.class);

        coreCompletionHandler.onSuccess("", responseModel);

        verify(abstractResponseHandler1).processResponse(responseModel);
        verify(abstractResponseHandler2).processResponse(responseModel);
    }

    @Test
    public void testAddResponseHandlers() {
        DefaultCompletionHandler coreCompletionHandler = new DefaultCompletionHandler(null);
        coreCompletionHandler.addResponseHandlers(handlers);

        assertEquals(handlers, coreCompletionHandler.responseHandlers);
    }

}