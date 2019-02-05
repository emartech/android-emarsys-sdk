package com.emarsys.core.request;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.connection.ConnectionProvider;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestTaskTest {

    private static final String WRONG_URL = "https://localhost/missing";
    private static final long TIMESTAMP_1 = 600;
    private static final long TIMESTAMP_2 = 1600;

    private RequestModel requestModel;

    private CoreCompletionHandler coreCompletionHandler;
    private ConnectionProvider connectionProvider;
    private TimestampProvider timestampProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        requestModel = mock(RequestModel.class);
        coreCompletionHandler = mock(CoreCompletionHandler.class);
        connectionProvider = new ConnectionProvider();
        timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP_1, TIMESTAMP_2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestModelMustNotBeNull() {
        new RequestTask(
                null,
                coreCompletionHandler,
                connectionProvider,
                timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_completionHandlerMustNotBeNull() {
        new RequestTask(
                requestModel,
                null,
                connectionProvider,
                timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_connectionProviderMustNotBeNull() {
        new RequestTask(
                requestModel,
                coreCompletionHandler,
                null,
                timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_timestampProviderMustNotBeNull() {
        new RequestTask(
                requestModel,
                coreCompletionHandler,
                connectionProvider,
                null);
    }

    @Test
    public void testDoInBackground_shouldBeResilientToRuntimeExceptions() throws IOException {
        String id = "123";
        connectionProvider = mock(ConnectionProvider.class);
        RequestModel requestModel = new RequestModel(
                WRONG_URL,
                RequestMethod.GET,
                null,
                new HashMap<String, String>(),
                400L,
                Long.MAX_VALUE,
                id);

        Exception runtimeException = new RuntimeException("Sneaky exception");
        HttpsURLConnection connection = mock(HttpsURLConnection.class);
        Mockito.doThrow(runtimeException).when(connection).connect();

        when(connectionProvider.provideConnection(requestModel)).thenReturn(connection);

        RequestTask requestTask = new RequestTask(requestModel, coreCompletionHandler, connectionProvider, timestampProvider);

        try {
            requestTask.doInBackground();
        } catch (Exception e) {
            Assert.fail("Request Task should handle exception: " + e.getMessage());
        }
    }

}