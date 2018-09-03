package com.emarsys.core.worker;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.model.CompositeRequestModel;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.specification.FilterByRequestId;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.test.util.HandlerUtils;
import com.emarsys.test.util.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class CoreCompletionHandlerMiddlewareTest {

    private CoreCompletionHandler coreCompletionHandler;
    private Repository<RequestModel, SqlSpecification> requestRepository;
    private Worker worker;

    private String expectedId;
    private CoreCompletionHandlerMiddleware middleware;
    private ArgumentCaptor<Message> captor;
    private Handler uiHandler;
    private Handler coreSdkHandler;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        expectedId = "expectedId";

        worker = mock(Worker.class);
        coreCompletionHandler = mock(CoreCompletionHandler.class);
        requestRepository = mock(Repository.class);
        uiHandler = new Handler(Looper.getMainLooper());
        coreSdkHandler = new CoreSdkHandlerProvider().provideHandler();
        middleware = new CoreCompletionHandlerMiddleware(worker, requestRepository, uiHandler, coreSdkHandler, coreCompletionHandler);
        captor = ArgumentCaptor.forClass(Message.class);
    }

    @Test
    public void testConstructor_handlerShouldNotBeNull() {
        assertNotNull(middleware.coreSDKHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_workerShouldNotBeNull() {
        new CoreCompletionHandlerMiddleware(null, requestRepository, uiHandler, coreSdkHandler, coreCompletionHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_queueShouldNotBeNull() {
        new CoreCompletionHandlerMiddleware(worker, null, uiHandler, coreSdkHandler, coreCompletionHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_coreCompletionHandlerShouldNotBeNull() {
        new CoreCompletionHandlerMiddleware(worker, requestRepository, uiHandler, coreSdkHandler, null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_uiHandlerShouldNotBeNull() {
        new CoreCompletionHandlerMiddleware(worker, requestRepository, null, coreSdkHandler, coreCompletionHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_coreHandlerShouldNotBeNull() {
        new CoreCompletionHandlerMiddleware(worker, requestRepository, uiHandler, null, coreCompletionHandler);
    }

    @Test
    public void testOnSuccess() {
        ArgumentCaptor<FilterByRequestId> captor = ArgumentCaptor.forClass(FilterByRequestId.class);

        ResponseModel expectedModel = createResponseModel(200);

        middleware.onSuccess(expectedId, expectedModel);

        HandlerUtils.waitForEventLoopToFinish(coreSdkHandler);
        HandlerUtils.waitForEventLoopToFinish(uiHandler);

        verify(worker).unlock();
        verify(worker).run();
        verifyNoMoreInteractions(worker);

        verify(requestRepository).remove(captor.capture());
        FilterByRequestId filter = captor.getValue();
        assertEquals(expectedModel.getRequestModel().getId(), filter.getArgs()[0]);

        verify(coreCompletionHandler).onSuccess(expectedId, expectedModel);
    }

    @Test
    public void testOnSuccess_callsHandlerPost() throws Exception {
        Handler handler = mock(Handler.class);
        middleware.coreSDKHandler = handler;

        middleware.onSuccess(expectedId, createResponseModel(200));

        verify(handler).sendMessageAtTime(captor.capture(), any(Long.class));

        Runnable runnable = captor.getValue().getCallback();
        runnable.run();
        verify(worker).run();
    }

    @Test
    public void testOnSuccess_withCompositeModel() {
        String[] ids = new String[]{"id1", "id2", "id3"};

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        RequestModel requestModel = new CompositeRequestModel(
                "https://emarsys.com",
                RequestMethod.POST,
                null,
                new HashMap<String, String>(),
                100,
                900000,
                ids);

        ResponseModel responseModel = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .headers(new HashMap<String, List<String>>())
                .body("{'key': 'value'}")
                .requestModel(requestModel)
                .build();

        middleware.onSuccess("0", responseModel);

        HandlerUtils.waitForEventLoopToFinish(coreSdkHandler);
        HandlerUtils.waitForEventLoopToFinish(uiHandler);

        verify(middleware.coreCompletionHandler, times(3)).onSuccess(captor.capture(), eq(responseModel));
        List<String> capturedIds = captor.getAllValues();
        assertEquals(Arrays.asList(ids), capturedIds);
    }

    @Test
    public void testOnError_4xx() {
        ArgumentCaptor<FilterByRequestId> captor = ArgumentCaptor.forClass(FilterByRequestId.class);

        ResponseModel expectedModel = createResponseModel(403);

        middleware.onError(expectedId, expectedModel);

        HandlerUtils.waitForEventLoopToFinish(coreSdkHandler);
        HandlerUtils.waitForEventLoopToFinish(uiHandler);

        verify(requestRepository).remove(captor.capture());
        FilterByRequestId filter = captor.getValue();
        assertEquals(expectedModel.getRequestModel().getId(), filter.getArgs()[0]);
        verify(coreCompletionHandler).onError(expectedId, expectedModel);

        verify(worker).unlock();
        verify(worker).run();
        verifyNoMoreInteractions(worker);
    }

    @Test
    public void testOnError_4xx_callsHandlerPost() {
        Handler handler = mock(Handler.class);
        middleware.coreSDKHandler = handler;

        middleware.onError(expectedId, createResponseModel(401));

        verify(handler).sendMessageAtTime(captor.capture(), any(Long.class));
        Runnable runnable = captor.getValue().getCallback();
        runnable.run();
        verify(worker).run();
    }

    @Test
    public void testOnError_408_shouldHandleErrorAsRetriable() {
        ResponseModel expectedModel = createResponseModel(408);

        middleware.onError(expectedId, expectedModel);

        HandlerUtils.waitForEventLoopToFinish(coreSdkHandler);
        HandlerUtils.waitForEventLoopToFinish(uiHandler);

        verify(worker).unlock();
        verifyNoMoreInteractions(worker);

        verifyZeroInteractions(coreCompletionHandler);
        verifyZeroInteractions(requestRepository);
    }

    @Test
    public void testOnError_5xx() {
        ResponseModel expectedModel = createResponseModel(500);

        middleware.onError(expectedId, expectedModel);

        HandlerUtils.waitForEventLoopToFinish(coreSdkHandler);
        HandlerUtils.waitForEventLoopToFinish(uiHandler);

        verify(worker).unlock();
        verifyNoMoreInteractions(worker);

        verifyZeroInteractions(coreCompletionHandler);
        verifyZeroInteractions(requestRepository);
    }

    @Test
    public void testOnError_4xx_withCompositeModel() {
        String[] ids = new String[]{"id1", "id2", "id3"};

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        RequestModel requestModel = new CompositeRequestModel(
                "https://emarsys.com",
                RequestMethod.POST,
                null,
                new HashMap<String, String>(),
                100,
                900000,
                ids);

        ResponseModel responseModel = new ResponseModel.Builder()
                .statusCode(400)
                .message("Bad Request")
                .headers(new HashMap<String, List<String>>())
                .body("{'key': 'value'}")
                .requestModel(requestModel)
                .build();

        middleware.onError("0", responseModel);

        HandlerUtils.waitForEventLoopToFinish(coreSdkHandler);
        HandlerUtils.waitForEventLoopToFinish(uiHandler);

        verify(middleware.coreCompletionHandler, times(3)).onError(captor.capture(), eq(responseModel));
        List<String> capturedIds = captor.getAllValues();
        assertEquals(Arrays.asList(ids), capturedIds);
    }

    @Test
    public void testOnError_withException() {
        Exception expectedException = new Exception("Expected exception");

        middleware.onError(expectedId, expectedException);

        HandlerUtils.waitForEventLoopToFinish(coreSdkHandler);
        HandlerUtils.waitForEventLoopToFinish(uiHandler);

        verify(worker).unlock();
        verifyNoMoreInteractions(worker);

        verify(coreCompletionHandler).onError(expectedId, expectedException);
        verifyNoMoreInteractions(coreCompletionHandler);

        verifyZeroInteractions(requestRepository);
    }

    private ResponseModel createResponseModel(int statusCode) {
        RequestModel requestModel = mock(RequestModel.class);
        when(requestModel.getId()).thenReturn(expectedId);

        return new ResponseModel.Builder()
                .statusCode(statusCode)
                .body("body")
                .message("message")
                .requestModel(requestModel)
                .build();
    }
}