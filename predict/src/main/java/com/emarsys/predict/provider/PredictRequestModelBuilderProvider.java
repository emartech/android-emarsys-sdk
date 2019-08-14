package com.emarsys.predict.provider;

import com.emarsys.core.util.Assert;
import com.emarsys.predict.request.PredictHeaderFactory;
import com.emarsys.predict.request.PredictRequestContext;
import com.emarsys.predict.request.PredictRequestModelBuilder;

public class PredictRequestModelBuilderProvider {

    private final PredictRequestContext requestContext;
    private final PredictHeaderFactory headerFactory;

    public PredictRequestModelBuilderProvider(PredictRequestContext requestContext, PredictHeaderFactory headerFactory) {
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(headerFactory, "HeaderFactory must not be null!");

        this.requestContext = requestContext;
        this.headerFactory = headerFactory;
    }

    public PredictRequestModelBuilder providePredictRequestModelBuilder() {
        return new PredictRequestModelBuilder(requestContext, headerFactory);
    }
}
