package com.emarsys.core.response;

import com.emarsys.core.request.model.RequestModel;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractResponseHandlerTest {

    private AbstractResponseHandler abstractResponseHandler;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        abstractResponseHandler = mock(AbstractResponseHandler.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test(expected = NullPointerException.class)
    public void testProcessResponse_responseShouldNotBeNull() {
        abstractResponseHandler.processResponse(null);
    }

    @Test
    public void testProcessResponse_shouldCallHandleResponse_whenResponseShouldBeHandled() {
        when(abstractResponseHandler.shouldHandleResponse(any(ResponseModel.class))).thenReturn(true);
        ResponseModel responseModel = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .requestModel(mock(RequestModel.class))
                .build();

        abstractResponseHandler.processResponse(responseModel);

        verify(abstractResponseHandler).handleResponse(responseModel);
    }

    @Test
    public void testProcessResponse_shouldNotCallHandleResponse_whenResponseShouldNotBeHandled() {
        when(abstractResponseHandler.shouldHandleResponse(any(ResponseModel.class))).thenReturn(false);
        ResponseModel responseModel = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .requestModel(mock(RequestModel.class))
                .build();

        abstractResponseHandler.processResponse(responseModel);

        verify(abstractResponseHandler, times(0)).handleResponse(responseModel);
    }
}