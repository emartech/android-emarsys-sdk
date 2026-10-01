package com.emarsys.predict.provider;

import com.emarsys.core.endpoint.ServiceEndpointProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.predict.request.PredictHeaderFactory;
import com.emarsys.predict.request.PredictRequestContext;
import com.emarsys.predict.request.PredictRequestModelBuilder;

public class PredictRequestModelBuilderProvider {

    private final PredictRequestContext requestContext;
    private final PredictHeaderFactory headerFactory;
    private final ServiceEndpointProvider predictServiceProvider;

    public PredictRequestModelBuilderProvider(PredictRequestContext requestContext, PredictHeaderFactory headerFactory, ServiceEndpointProvider predictServiceProvider) {
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(headerFactory, "HeaderFactory must not be null!");
        Assert.notNull(predictServiceProvider, "PredictServiceProvider must not be null!");

        this.requestContext = requestContext;
        this.headerFactory = headerFactory;
        this.predictServiceProvider = predictServiceProvider;
    }

    public PredictRequestModelBuilder providePredictRequestModelBuilder() {
        return new PredictRequestModelBuilder(requestContext, headerFactory, predictServiceProvider);
    }
}
