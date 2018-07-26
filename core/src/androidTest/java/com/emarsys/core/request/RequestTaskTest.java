package com.emarsys.core.request;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.testUtil.TimeoutUtils;
import com.emarsys.core.timestamp.TimestampProvider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class RequestTaskTest {

    private static final String DENNA_ECHO_URL = "https://ems-denna.herokuapp.com/echo";
    private static final String WRONG_URL = "https://localhost/missing";
    public static final String NETWORKING_TIME = "networking_time";
    public static final String IN_DATABASE = "in_database_time";
    private static final long TIMESTAMP_1 = 600;
    private static final long TIMESTAMP_2 = 1600;

    private RequestModel requestModel;

    private CoreCompletionHandler coreCompletionHandler;
    private Repository<Map<String, Object>, SqlSpecification> logRepository;
    private TimestampProvider timestampProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        requestModel = mock(RequestModel.class);
        coreCompletionHandler = mock(CoreCompletionHandler.class);
        logRepository = mock(Repository.class);
        timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP_1, TIMESTAMP_2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestModelMustNotBeNull() {
        new RequestTask(null, coreCompletionHandler, logRepository, timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_completionHandlerMustNotBeNull() {
        new RequestTask(requestModel, null, logRepository, timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_logRepositoryMustNotBeNull() {
        new RequestTask(requestModel, coreCompletionHandler, null, timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_timestampProviderMustNotBeNull() {
        new RequestTask(requestModel, coreCompletionHandler, logRepository, null);
    }

    @Test
    public void testDoInBackground_savesInDatabaseTime() {
        RequestModel requestModel = new RequestModel(DENNA_ECHO_URL,
                RequestMethod.GET,
                null,
                new HashMap<String, String>(),
                400L,
                Long.MAX_VALUE,
                "123");

        RequestTask requestTask = new RequestTask(requestModel, coreCompletionHandler, logRepository, timestampProvider);

        requestTask.doInBackground();

        Map<String, Object> inDbMetric = createMetricFromRequestModel(
                requestModel,
                IN_DATABASE,
                requestModel.getTimestamp(),
                TIMESTAMP_1);

        verify(logRepository).add(inDbMetric);
    }

    @Test
    public void testDoInBackground_savesNetworkingTime() {
        RequestModel requestModel = new RequestModel(DENNA_ECHO_URL,
                RequestMethod.GET,
                null,
                new HashMap<String, String>(),
                400L,
                Long.MAX_VALUE,
                "123");

        RequestTask requestTask = new RequestTask(requestModel, coreCompletionHandler, logRepository, timestampProvider);

        requestTask.doInBackground();

        Map<String, Object> networkingMetric = createMetricFromRequestModel(
                requestModel,
                NETWORKING_TIME,
                TIMESTAMP_1,
                TIMESTAMP_2);

        verify(logRepository).add(networkingMetric);
    }

    @Test
    public void testDoInBackground_savesOnlyInDatabaseTime_inCaseOfNetworkingError() {
        RequestModel requestModel = new RequestModel(
                WRONG_URL,
                RequestMethod.GET,
                null,
                new HashMap<String, String>(),
                400L,
                Long.MAX_VALUE,
                "123");

        RequestTask requestTask = new RequestTask(requestModel, coreCompletionHandler, logRepository, timestampProvider);

        requestTask.doInBackground();

        Map<String, Object> inDbMetric = createMetricFromRequestModel(
                requestModel,
                IN_DATABASE,
                requestModel.getTimestamp(),
                TIMESTAMP_1);

        verify(logRepository).add(inDbMetric);
        verifyNoMoreInteractions(logRepository);
    }

    private Map<String, Object> createMetricFromRequestModel(RequestModel requestModel, String metricName, long start, long end) {
        HashMap<String, Object> metric = new HashMap<>();
        metric.put("request_id", requestModel.getId());
        metric.put(metricName, end - start);
        metric.put("url", requestModel.getUrl().toString());
        return metric;
    }

}