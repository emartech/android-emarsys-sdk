package com.emarsys.predict.request;

import android.net.Uri;

import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.predict.api.model.Logic;
import com.emarsys.predict.api.model.RecommendationLogic;
import com.emarsys.predict.endpoint.Endpoint;
import com.emarsys.predict.model.LastTrackedItemContainer;

import java.util.Map;

public class PredictRequestModelBuilder {
    private static final int DEFAULT_LIMIT = 5;

    private final PredictRequestContext requestContext;
    private final PredictHeaderFactory headerFactory;
    private Map<String, Object> shardData;
    private Logic logic;
    private LastTrackedItemContainer lastTrackedItemContainer;
    private Integer limit;

    public PredictRequestModelBuilder(PredictRequestContext requestContext, PredictHeaderFactory headerFactory) {
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(headerFactory, "HeaderFactory must not be null!");

        this.requestContext = requestContext;
        this.headerFactory = headerFactory;
    }

    public PredictRequestModelBuilder withShardData(Map<String, Object> shardData) {
        Assert.notNull(shardData, "ShardData must not be null!");
        this.shardData = shardData;
        return this;
    }

    public PredictRequestModelBuilder withLogic(Logic logic, LastTrackedItemContainer lastTrackedItemContainer) {
        Assert.notNull(logic, "Logic must not be null!");
        Assert.notNull(lastTrackedItemContainer, "LastTrackedItemContainer must not be null!");
        this.logic = logic;
        this.lastTrackedItemContainer = lastTrackedItemContainer;
        return this;
    }

    public PredictRequestModelBuilder withLimit(Integer limit) {
        if (limit != null && limit < 1) {
            throw new IllegalArgumentException("Limit must be greater than zero or Null!");
        }
        this.limit = limit;
        return this;
    }

    public RequestModel build() {
        RequestModel.Builder requestModelBuilder = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUuidProvider())
                .method(RequestMethod.GET)
                .headers(headerFactory.createBaseHeader());
        if (logic != null) {
            requestModelBuilder.url(createRecommendationUrl(logic));
        } else {
            requestModelBuilder.url(createUrl(shardData));
        }
        return requestModelBuilder.build();
    }

    private String createRecommendationUrl(Logic logic) {
        Uri.Builder uriBuilder = Uri.parse(Endpoint.PREDICT_BASE_URL)
                .buildUpon()
                .appendPath(requestContext.getMerchantId());
        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
        uriBuilder.appendQueryParameter("f", "f:" + logic.getLogicName() + ",l:" + limit + ",o:0");

        if (logic.getData().isEmpty()) {
            switch (logic.getLogicName()) {
                case RecommendationLogic.SEARCH:
                    if (lastTrackedItemContainer.getLastSearchTerm() != null) {
                        logic.getData().putAll(RecommendationLogic.search(lastTrackedItemContainer.getLastSearchTerm()).getData());
                    }
                    break;
                case RecommendationLogic.CART:
                    if (lastTrackedItemContainer.getLastCartItems() != null) {
                        logic.getData().putAll(RecommendationLogic.cart(lastTrackedItemContainer.getLastCartItems()).getData());
                    }
                    break;
                case RecommendationLogic.CATEGORY:
                    if (lastTrackedItemContainer.getLastCategoryPath() != null) {
                        logic.getData().putAll(RecommendationLogic.category(lastTrackedItemContainer.getLastCategoryPath()).getData());
                    }
                    break;
                case RecommendationLogic.POPULAR:
                    if (lastTrackedItemContainer.getLastCategoryPath() != null) {
                        logic.getData().putAll(RecommendationLogic.popular(lastTrackedItemContainer.getLastCategoryPath()).getData());
                    }
                    break;
                case RecommendationLogic.RELATED:
                    if (lastTrackedItemContainer.getLastItemView() != null) {
                        logic.getData().putAll(RecommendationLogic.related(lastTrackedItemContainer.getLastItemView()).getData());
                    }
                    break;
                case RecommendationLogic.ALSO_BOUGHT:
                    if (lastTrackedItemContainer.getLastItemView() != null) {
                        logic.getData().putAll(RecommendationLogic.alsoBought(lastTrackedItemContainer.getLastItemView()).getData());
                    }
                    break;
            }
        }
        for (String key : logic.getData().keySet()) {
            uriBuilder.appendQueryParameter(key, logic.getData().get(key));
        }
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
