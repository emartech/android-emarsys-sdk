package com.emarsys.core.request.model;

import java.util.Arrays;
import java.util.Map;

public class CompositeRequestModel extends RequestModel {

    private final String[] originalRequestIds;

    public CompositeRequestModel(
            String url,
            RequestMethod method,
            Map<String, Object> payload,
            Map<String, String> headers,
            long timestamp,
            long ttl,
            String[] originalRequestIds) {
        super(url, method, payload, headers, timestamp, ttl, "0");
        this.originalRequestIds = originalRequestIds;
    }

    public CompositeRequestModel(
            String url,
            RequestMethod method,
            Map<String, Object> payload,
            Map<String, String> headers,
            String[] originalRequestIds) {
        this(url, method, payload, headers, System.currentTimeMillis(), Long.MAX_VALUE, originalRequestIds);
    }

    public String[] getOriginalRequestIds() {
        return originalRequestIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CompositeRequestModel that = (CompositeRequestModel) o;

        return Arrays.equals(originalRequestIds, that.originalRequestIds);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(originalRequestIds);
        return result;
    }

    @Override
    public String toString() {
        return "CompositeRequestModel{" +
                "request=" + super.toString() +
                "originalRequestIds=" + Arrays.toString(originalRequestIds) +
                '}';
    }
}
