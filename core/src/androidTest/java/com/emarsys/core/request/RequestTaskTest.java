package com.emarsys.core.request;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.Mapper;
import com.emarsys.core.connection.ConnectionProvider;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseHandlersProcessor;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestTaskTest {

    private static final String WRONG_URL = "https://localhost/missing";
    private static final String URL = "https://emarsys.com";
    private static final long TIMESTAMP_1 = 600;
    private static final long TIMESTAMP_2 = 1600;

    private RequestModel requestModel;

    private CoreCompletionHandler coreCompletionHandler;
    private ConnectionProvider connectionProvider;
    private TimestampProvider timestampProvider;
    private ResponseHandlersProcessor mockResponseHandlersProcessor;
    private List<Mapper<RequestModel, RequestModel>> requestModelMappers;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        requestModel = mock(RequestModel.class);
        coreCompletionHandler = mock(CoreCompletionHandler.class);
        connectionProvider = new ConnectionProvider();
        timestampProvider = mock(TimestampProvider.class);
        mockResponseHandlersProcessor = mock(ResponseHandlersProcessor.class);
        requestModelMappers = new ArrayList<>();
        when(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP_1, TIMESTAMP_2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestModelMustNotBeNull() {
        new RequestTask(
                null,
                coreCompletionHandler,
                connectionProvider,
                timestampProvider,
                mockResponseHandlersProcessor,
                requestModelMappers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_completionHandlerMustNotBeNull() {
        new RequestTask(
                requestModel,
                null,
                connectionProvider,
                timestampProvider,
                mockResponseHandlersProcessor,
                requestModelMappers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_connectionProviderMustNotBeNull() {
        new RequestTask(
                requestModel,
                coreCompletionHandler,
                null,
                timestampProvider,
                mockResponseHandlersProcessor,
                requestModelMappers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_timestampProviderMustNotBeNull() {
        new RequestTask(
                requestModel,
                coreCompletionHandler,
                connectionProvider,
                null,
                mockResponseHandlersProcessor,
                requestModelMappers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_responseHandlersRunnerMustNotBeNull() {
        new RequestTask(
                requestModel,
                coreCompletionHandler,
                connectionProvider,
                timestampProvider,
                null,
                requestModelMappers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestMappersMustNotBeNull() {
        new RequestTask(
                requestModel,
                coreCompletionHandler,
                connectionProvider,
                timestampProvider,
                mockResponseHandlersProcessor,
                null);
    }

    @Test
    public void testDoInBackground_shouldBeResilientToRuntimeExceptions() throws IOException {
        connectionProvider = mock(ConnectionProvider.class);
        RequestModel requestModel = mock(RequestModel.class);
        when(requestModel.getUrl()).thenReturn(new URL(WRONG_URL));

        Exception runtimeException = new RuntimeException("Sneaky exception");
        HttpsURLConnection connection = mock(HttpsURLConnection.class);
        Mockito.doThrow(runtimeException).when(connection).connect();

        when(connectionProvider.provideConnection(requestModel)).thenReturn(connection);

        RequestTask requestTask = new RequestTask(requestModel, coreCompletionHandler, connectionProvider, timestampProvider, mockResponseHandlersProcessor, requestModelMappers);

        try {
            requestTask.doInBackground();
        } catch (Exception e) {
            Assert.fail("Request Task should handle exception: " + e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDoInBackground_mappersHaveBeenCalled() throws IOException {
        connectionProvider = mock(ConnectionProvider.class);
        RequestModel requestModel = mock(RequestModel.class);
        when(requestModel.getUrl()).thenReturn(new URL(URL));

        HttpsURLConnection connection = mock(HttpsURLConnection.class);
        Mapper<RequestModel, RequestModel> mapper1 = mock(Mapper.class);
        Mapper<RequestModel, RequestModel> mapper2 = mock(Mapper.class);
        RequestModel expectedRequestModel1 = mock(RequestModel.class);
        RequestModel expectedRequestModel2 = mock(RequestModel.class);

        requestModelMappers.add(mapper1);
        requestModelMappers.add(mapper2);

        when(mapper1.map(requestModel)).thenReturn(expectedRequestModel1);
        when(mapper2.map(expectedRequestModel1)).thenReturn(expectedRequestModel2);

        when(connectionProvider.provideConnection(expectedRequestModel2)).thenReturn(connection);

        RequestTask requestTask = new RequestTask(requestModel, coreCompletionHandler, connectionProvider, timestampProvider, mockResponseHandlersProcessor, requestModelMappers);

        requestTask.doInBackground();

        verify(mapper1).map(requestModel);
        verify(mapper2).map(expectedRequestModel1);
        verify(connectionProvider).provideConnection(expectedRequestModel2);
    }
}