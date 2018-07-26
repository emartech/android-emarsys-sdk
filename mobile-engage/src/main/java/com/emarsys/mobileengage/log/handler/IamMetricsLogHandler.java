package com.emarsys.mobileengage.log.handler;

import com.emarsys.core.handler.Handler;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.CollectionUtils;
import com.emarsys.mobileengage.util.RequestUrlUtils;

import java.util.Collections;
import java.util.Map;

public class IamMetricsLogHandler implements Handler<Map<String, Object>, Map<String, Object>> {
    private static final String REQUEST_ID = "request_id";
    private static final String CAMPAIGN_ID = "campaign_id";
    private static final String URL = "url";
    private static final String IN_DATABASE = "in_database_time";
    private static final String NETWORKING_TIME = "networking_time";
    private static final String LOADING_TIME = "loading_time";

    private final Map<String, Map<String, Object>> metricsBuffer;

    public IamMetricsLogHandler(Map<String, Map<String, Object>> metricsBuffer) {
        Assert.notNull(metricsBuffer, "MetricsBuffer must not be null!");
        this.metricsBuffer = metricsBuffer;
    }

    @Override
    public Map<String, Object> handle(Map<String, Object> item) {
        Assert.notNull(item, "Item must not be null!");
        Map<String, Object> completeMetric = null;
        if (hasValidId(item)
                && (isInDatabaseMetric(item) || isNetworkingTimeMetric(item) || isLoadingTimeMetric(item))) {

            String id = (String) item.get(REQUEST_ID);
            updateBuffer(id, item);
            if (isMetricComplete(id)) {
                completeMetric = metricsBuffer.get(id);
                metricsBuffer.remove(id);
            }
        }
        return completeMetric;
    }

    private boolean isMetricComplete(String id) {
        Map<String, Object> metric = metricsBuffer.get(id);
        return metric != null
                && metric.containsKey(IN_DATABASE)
                && metric.containsKey(NETWORKING_TIME)
                && metric.containsKey(LOADING_TIME)
                && metric.containsKey(CAMPAIGN_ID);
    }

    private boolean isInDatabaseMetric(Map<String, Object> item) {
        return hasValidCustomEventUrl(item) && hasInDatabase(item);
    }

    private boolean isNetworkingTimeMetric(Map<String, Object> item) {
        return hasValidCustomEventUrl(item) && hasNetworkingTime(item);
    }

    private boolean isLoadingTimeMetric(Map<String, Object> item) {
        return item.containsKey(LOADING_TIME) && item.containsKey(CAMPAIGN_ID);
    }

    private boolean hasValidId(Map<String, Object> item) {
        return item.containsKey(REQUEST_ID) && item.get(REQUEST_ID) instanceof String;
    }

    private boolean hasValidCustomEventUrl(Map<String, Object> item) {
        return item.containsKey(URL)
                && item.get(URL) instanceof String
                && RequestUrlUtils.isCustomEvent_V3((String) item.get(URL));
    }

    private boolean hasInDatabase(Map<String, Object> item) {
        return item.containsKey(IN_DATABASE);
    }

    private boolean hasNetworkingTime(Map<String, Object> item) {
        return item.containsKey(NETWORKING_TIME);
    }

    @SuppressWarnings("unchecked")
    private void updateBuffer(String id, Map<String, Object> item) {
        Map<String, Object> currentMetric = metricsBuffer.get(id);
        currentMetric = currentMetric == null ? Collections.<String, Object>emptyMap() : currentMetric;

        metricsBuffer.put(id, CollectionUtils.mergeMaps(currentMetric, item));

    }
}