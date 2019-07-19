package com.emarsys.predict.request;

import android.net.Uri;

import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.predict.endpoint.Endpoint;

import java.util.Map;

public class PredictRequestModelFactory {

    private final PredictRequestContext requestContext;
    private final PredictHeaderFactory headerFactory;

    public PredictRequestModelFactory(PredictRequestContext requestContext, PredictHeaderFactory headerFactory) {
        Assert.notNull(requestContext, "PredictRequestContext must not be null!");
        Assert.notNull(headerFactory, "PredictHeaderFactory must not be null!");
        this.requestContext = requestContext;
        this.headerFactory = headerFactory;
    }

    public RequestModel createRecommendationRequest() {
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUuidProvider())
                .url(createRecommendationUrl())
                .method(RequestMethod.GET)
                .headers(headerFactory.createBaseHeader())
                .build();
    }

    public RequestModel createRequestFromShardData(Map<String, Object> shardData) {
        return new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUuidProvider())
                .url(createUrl(shardData))
                .method(RequestMethod.GET)
                .headers(headerFactory.createBaseHeader())
                .build();
    }

    private String createRecommendationUrl() {
        Uri.Builder uriBuilder = Uri.parse(Endpoint.PREDICT_BASE_URL)
                .buildUpon()
                .appendPath(requestContext.getMerchantId());

        uriBuilder.appendQueryParameter("f", "f:SEARCH,l:5,o:0");
        uriBuilder.appendQueryParameter("q", "polo shirt");

        return uriBuilder.build().toString();
    }

    private String createUrl(Map<String, Object> shardData) {
        Uri.Builder uriBuilder = Uri.parse(Endpoint.PREDICT_BASE_URL)
                .buildUpon()
                .appendPath(requestContext.getMerchantId());

        for (String key : shardData.keySet()) {
            uriBuilder.appendQueryParameter(key, shardData.get(key).toString());
        }

        return uriBuilder.build().toString();
    }
}
