package com.emarsys.core.request.model;

import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.util.Assert;

import java.util.Arrays;
import java.util.Map;

public class CompositeRequestModel extends RequestModel {

    private final String[] originalRequestIds;

    public CompositeRequestModel(
            String id,
            String url,
            RequestMethod method,
            Map<String, Object> payload,
            Map<String, String> headers,
            long timestamp,
            long ttl,
            String[] originalRequestIds) {
        super(url, method, payload, headers, timestamp, ttl, id);
        this.originalRequestIds = originalRequestIds;
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

    public static class Builder extends RequestModel.Builder {
        private String[] originalRequestIds;

        public Builder(TimestampProvider timestampProvider, UUIDProvider uuidProvider) {
            super(timestampProvider, uuidProvider);
        }

        public Builder(RequestModel requestModel) {
            super(requestModel);
            Assert.notNull(requestModel, "RequestModel must not be null!");
            this.url = requestModel.getUrl().toString();
            this.headers = requestModel.getHeaders();
            this.method = requestModel.getMethod();
            this.originalRequestIds = ((CompositeRequestModel) requestModel).getOriginalRequestIds();
            this.payload = requestModel.getPayload();
            this.timestamp = requestModel.getTimestamp();
            this.ttl = requestModel.getTtl();
        }

        @Override
        public CompositeRequestModel.Builder url(String url) {
            super.url(url);
            return this;
        }

        @Override
        public CompositeRequestModel.Builder queryParams(Map<String, String> queryParams) {
            super.queryParams(queryParams);
            return this;
        }

        @Override
        public CompositeRequestModel.Builder method(RequestMethod method) {
            super.method(method);
            return this;
        }

        @Override
        public CompositeRequestModel.Builder payload(Map<String, Object> payload) {
            super.payload(payload);
            return this;
        }

        @Override
        public CompositeRequestModel.Builder headers(Map<String, String> headers) {
            super.headers(headers);
            return this;
        }

        @Override
        public CompositeRequestModel.Builder ttl(long ttl) {
            super.ttl(ttl);
            return this;
        }

        public CompositeRequestModel.Builder originalRequestIds(String[] originalRequestIds) {
            this.originalRequestIds = originalRequestIds;
            return this;
        }

        public CompositeRequestModel build() {
            return new CompositeRequestModel(id, buildUrl(), method, payload, headers, timestamp, ttl, originalRequestIds);
        }
    }
}
