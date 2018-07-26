package com.emarsys.mobileengage.log.handler;

import com.emarsys.core.util.CollectionUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class IamMetricsLogHandlerTest {
    private static final String CUSTOM_EVENT_V3_URL = "https://mobile-events.eservice.emarsys.net/v3/devices/123456789/events";
    private static final String NOT_CUSTOM_EVENT_V3_URL = "https://push.eservice.emarsys.net/api/mobileengage/v2/events/mycustomevent";
    private static final String REQUEST_ID = "request_id";
    private static final String URL = "url";
    private static final String IN_DATABASE = "in_database_time";
    private static final String NETWORKING_TIME = "networking_time";
    private static final String LOADING_TIME = "loading_time";
    private static final String ON_SCREEN_TIME = "on_screen_time";
    private static final String CAMPAIGN_ID = "campaign_id";

    private IamMetricsLogHandler handlerWithMockBuffer;
    private Map<String, Map<String, Object>> mockMetricsBuffer;

    private IamMetricsLogHandler handler;
    private Map<String, Map<String, Object>> metricsBuffer;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        mockMetricsBuffer = mock(HashMap.class, Mockito.CALLS_REAL_METHODS);
        handlerWithMockBuffer = new IamMetricsLogHandler(mockMetricsBuffer);

        metricsBuffer = new HashMap<>();
        handler = new IamMetricsLogHandler(metricsBuffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_metricsBuffer_mustNotBeNull() {
        new IamMetricsLogHandler(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandle_itemMustNotBeNull() {
        handlerWithMockBuffer.handle(null);
    }

    @Test
    public void testHandle_doesNotStore_anyMetric_ifRequestIdIsMissing() {
        Map<String, Object> metric = new HashMap<>();
        metric.put("url", CUSTOM_EVENT_V3_URL);
        metric.put(IN_DATABASE, 100);
        metric.put(NETWORKING_TIME, 200);
        metric.put(LOADING_TIME, 200);
        metric.put(ON_SCREEN_TIME, 200);

        handlerWithMockBuffer.handle(metric);

        verifyZeroInteractions(mockMetricsBuffer);
    }

    @Test
    public void testHandle_doesNotStore_anyMetric_ifRequestId_isNotString() {
        Map<String, Object> metric = new HashMap<>();
        metric.put("url", CUSTOM_EVENT_V3_URL);
        metric.put(IN_DATABASE, 100);
        metric.put(NETWORKING_TIME, 200);
        metric.put(REQUEST_ID, 200_00_00);
        metric.put(LOADING_TIME, 200);
        metric.put(ON_SCREEN_TIME, 200);

        handlerWithMockBuffer.handle(metric);

        verifyZeroInteractions(mockMetricsBuffer);
    }

    @Test
    public void testHandle_storesMetric_onlyIfContains_metricKey() {
        Map<String, Object> metric = new HashMap<>();
        metric.put(REQUEST_ID, "hash");
        metric.put(URL, CUSTOM_EVENT_V3_URL);

        handlerWithMockBuffer.handle(metric);

        verifyZeroInteractions(mockMetricsBuffer);
    }

    @Test
    public void testHandle_doesNotStore_inDatabaseMetric_ifUrlIsMissing() {
        Map<String, Object> metric = new HashMap<>();
        metric.put(IN_DATABASE, 123);
        metric.put(REQUEST_ID, "hash");

        handlerWithMockBuffer.handle(metric);

        verifyZeroInteractions(mockMetricsBuffer);
    }

    @Test
    public void testHandle_doesNotStore_inDatabaseMetric_ifUrl_isNotString() {
        Map<String, Object> metric = new HashMap<>();
        metric.put(IN_DATABASE, 123);
        metric.put(REQUEST_ID, "hash");
        metric.put(URL, Math.PI);

        handlerWithMockBuffer.handle(metric);

        verifyZeroInteractions(mockMetricsBuffer);
    }

    @Test
    public void testHandle_doesNotStore_inDatabaseMetric_ifUrlIsNot_V3CustomEvent() {
        Map<String, Object> metric = new HashMap<>();
        metric.put(IN_DATABASE, 123);
        metric.put(REQUEST_ID, "hash");
        metric.put(URL, NOT_CUSTOM_EVENT_V3_URL);

        handlerWithMockBuffer.handle(metric);

        verifyZeroInteractions(mockMetricsBuffer);
    }

    @Test
    public void testHandle_storesInDatabaseMetric() {
        Map<String, Object> input = new HashMap<>();
        input.put(IN_DATABASE, 200);
        input.put(REQUEST_ID, "id");
        input.put(URL, CUSTOM_EVENT_V3_URL);

        Map<String, Object> expectedStoredMetric = new HashMap<>(input);

        handler.handle(input);
        Assert.assertEquals(expectedStoredMetric, metricsBuffer.get("id"));
    }

    @Test
    public void testHandle_inDatabaseMetric_doesNotReturnIncompleteMetric() {
        Map<String, Object> input = new HashMap<>();
        input.put(IN_DATABASE, 200);
        input.put(REQUEST_ID, "id");
        input.put(URL, CUSTOM_EVENT_V3_URL);

        Assert.assertNull(handler.handle(input));
    }

    @Test
    public void testHandle_doesNotStore_networkingTimeMetric_ifUrlIsMissing() {
        Map<String, Object> metric = new HashMap<>();
        metric.put(NETWORKING_TIME, 1200);
        metric.put(REQUEST_ID, "hash");

        handlerWithMockBuffer.handle(metric);

        verifyZeroInteractions(mockMetricsBuffer);
    }

    @Test
    public void testHandle_doesNotStore_networkingTimeMetric_ifUrl_isNotString() {
        Map<String, Object> metric = new HashMap<>();
        metric.put(NETWORKING_TIME, 1200);
        metric.put(REQUEST_ID, "hash");
        metric.put(URL, Math.PI);

        handlerWithMockBuffer.handle(metric);

        verifyZeroInteractions(mockMetricsBuffer);
    }

    @Test
    public void testHandle_doesNotStore_networkingTimeMetric_ifUrlIsNot_V3CustomEvent() {
        Map<String, Object> metric = new HashMap<>();
        metric.put(NETWORKING_TIME, 1200);
        metric.put(REQUEST_ID, "hash");
        metric.put(URL, NOT_CUSTOM_EVENT_V3_URL);

        handlerWithMockBuffer.handle(metric);

        verifyZeroInteractions(mockMetricsBuffer);
    }

    @Test
    public void testHandle_storesNetworkingTimeMetric() {
        Map<String, Object> input = new HashMap<>();
        input.put(NETWORKING_TIME, 1200);
        input.put(REQUEST_ID, "id");
        input.put(URL, CUSTOM_EVENT_V3_URL);

        handler.handle(input);
        Map<String, Object> expectedStoredMetric = new HashMap<>(input);

        Assert.assertEquals(expectedStoredMetric, metricsBuffer.get("id"));
    }

    @Test
    public void testHandle_networkingTime_doesNotReturnIncompleteMetric() {
        Map<String, Object> input = new HashMap<>();
        input.put(NETWORKING_TIME, 1200);
        input.put(REQUEST_ID, "id");
        input.put(URL, CUSTOM_EVENT_V3_URL);

        Assert.assertNull(handler.handle(input));
    }

    @Test
    public void testHandle_doesNotStore_loadingTimeMetric_ifCampaignIdIsMissing() {
        Map<String, Object> input = new HashMap<>();
        input.put(LOADING_TIME, 1200);
        input.put(REQUEST_ID, "id");

        handlerWithMockBuffer.handle(input);

        verifyZeroInteractions(mockMetricsBuffer);
    }

    @Test
    public void testHandle_storesLoadingTimeMetric() {
        Map<String, Object> input = new HashMap<>();
        input.put(LOADING_TIME, 200);
        input.put(REQUEST_ID, "id");
        input.put(CAMPAIGN_ID, "campaign_id");

        Map<String, Object> expectedStoredMetric = new HashMap<>(input);

        handler.handle(input);
        Assert.assertEquals(expectedStoredMetric, metricsBuffer.get("id"));
    }

    @Test
    public void testHandle_loadingTime_doesNotReturnIncompleteMetric() {
        Map<String, Object> input = new HashMap<>();
        input.put(LOADING_TIME, 200);
        input.put(REQUEST_ID, "id");

        Assert.assertNull(handler.handle(input));
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testHandle_returnsCompleteMetric() {
        Map<String, Object> inDatabase = new HashMap<>();
        inDatabase.put(IN_DATABASE, 200);
        inDatabase.put(REQUEST_ID, "id");
        inDatabase.put(URL, CUSTOM_EVENT_V3_URL);

        Map<String, Object> networkingTime = new HashMap<>();
        networkingTime.put(NETWORKING_TIME, 1200);
        networkingTime.put(REQUEST_ID, "id");
        networkingTime.put(URL, CUSTOM_EVENT_V3_URL);

        Map<String, Object> loadingTime = new HashMap<>();
        loadingTime.put(LOADING_TIME, 1200);
        loadingTime.put(REQUEST_ID, "id");
        loadingTime.put(CAMPAIGN_ID, "campaignId");

        Map<String, Object> merged = CollectionUtils.mergeMaps(inDatabase, networkingTime, loadingTime);

        Assert.assertNull(handler.handle(inDatabase));
        Assert.assertNull(handler.handle(networkingTime));
        Assert.assertEquals(merged, handler.handle(loadingTime));
    }


    @Test
    public void testHandle_removesMetricFromBuffer_afterReturningCompleteMetric() {
        Map<String, Object> inDatabase = new HashMap<>();
        inDatabase.put(IN_DATABASE, 200);
        inDatabase.put(REQUEST_ID, "id");
        inDatabase.put(URL, CUSTOM_EVENT_V3_URL);

        Map<String, Object> networkingTime = new HashMap<>();
        networkingTime.put(NETWORKING_TIME, 1200);
        networkingTime.put(REQUEST_ID, "id");
        networkingTime.put(URL, CUSTOM_EVENT_V3_URL);

        Map<String, Object> loadingTime = new HashMap<>();
        loadingTime.put(LOADING_TIME, 1200);
        loadingTime.put(REQUEST_ID, "id");
        loadingTime.put(CAMPAIGN_ID, "campaignId");

        handler.handle(inDatabase);
        Assert.assertEquals(1, metricsBuffer.size());

        handler.handle(networkingTime);
        Assert.assertEquals(1, metricsBuffer.size());

        handler.handle(loadingTime);
        Assert.assertEquals(0, metricsBuffer.size());
    }

    @Test
    public void testHandle_removeMetricFromBuffer_afterReturningCompleteMetric_andKeepsOtherMetrics() {
        String id2 = "id2";

        Map<String, Object> inDatabase = new HashMap<>();
        inDatabase.put(IN_DATABASE, 200);
        inDatabase.put(REQUEST_ID, "id");
        inDatabase.put(URL, CUSTOM_EVENT_V3_URL);

        Map<String, Object> networkingTime = new HashMap<>();
        networkingTime.put(NETWORKING_TIME, 1200);
        networkingTime.put(REQUEST_ID, "id");
        networkingTime.put(URL, CUSTOM_EVENT_V3_URL);

        Map<String, Object> loadingTime = new HashMap<>();
        loadingTime.put(LOADING_TIME, 1700);
        loadingTime.put(REQUEST_ID, "id");
        loadingTime.put(CAMPAIGN_ID, "campaignId");

        Map<String, Object> inDatabase2 = new HashMap<>();
        inDatabase2.put(IN_DATABASE, 500);
        inDatabase2.put(REQUEST_ID, id2);
        inDatabase2.put(URL, CUSTOM_EVENT_V3_URL);
        handler.handle(inDatabase2);

        handler.handle(inDatabase);
        Assert.assertEquals(2, metricsBuffer.size());

        handler.handle(networkingTime);
        Assert.assertEquals(2, metricsBuffer.size());

        handler.handle(loadingTime);
        Assert.assertEquals(1, metricsBuffer.size());

        Assert.assertEquals(inDatabase2, metricsBuffer.get(id2));
    }
}