package com.emarsys.predict.request;

import android.net.Uri;
import android.text.TextUtils;

import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.JsonUtils;
import com.emarsys.predict.api.model.Logic;
import com.emarsys.predict.api.model.LogicData;
import com.emarsys.predict.api.model.RecommendationFilter;
import com.emarsys.predict.api.model.RecommendationLogic;
import com.emarsys.predict.endpoint.Endpoint;
import com.emarsys.predict.model.LastTrackedItemContainer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PredictRequestModelBuilder {
    private static final int DEFAULT_LIMIT = 5;

    private final PredictRequestContext requestContext;
    private final PredictHeaderFactory headerFactory;
    private Map<String, Object> shardData;
    private Logic logic;
    private LastTrackedItemContainer lastTrackedItemContainer;
    private Integer limit;
    private List<RecommendationFilter> filters;
    private Uri.Builder uriBuilder;

    public PredictRequestModelBuilder(PredictRequestContext requestContext, PredictHeaderFactory headerFactory) {
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(headerFactory, "HeaderFactory must not be null!");

        this.requestContext = requestContext;
        this.headerFactory = headerFactory;

        uriBuilder = Uri.parse(Endpoint.PREDICT_BASE_URL)
                .buildUpon()
                .appendPath(requestContext.getMerchantId());
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

    public PredictRequestModelBuilder withFilters(List<RecommendationFilter> recommendationFilters) {
        this.filters = recommendationFilters;
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

        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
        String url;
        if (RecommendationLogic.PERSONAL.equals(logic.getLogicName())) {
            url = createPersonalRecommendationUrl(logic);
        } else {
            url = createNonPersonalLogicUrl(logic);
        }

        uriBuilder.clearQuery();
        return url;
    }

    private String createNonPersonalLogicUrl(Logic logic) {
        uriBuilder.appendQueryParameter("f", "f:" + logic.getLogicName() + ",l:" + limit + ",o:0");

        if (this.filters != null) {
            uriBuilder.appendQueryParameter("ex", createRecommendationFilterQueryValues());
        }

        Map<String, String> data = logic.getData().getLogicData();

        if (data.isEmpty()) {
            switch (logic.getLogicName()) {
                case RecommendationLogic.SEARCH:
                    if (lastTrackedItemContainer.getLastSearchTerm() != null) {
                        data.putAll(RecommendationLogic.search(lastTrackedItemContainer.getLastSearchTerm()).getData().getLogicData());
                    }
                    break;
                case RecommendationLogic.CART:
                    if (lastTrackedItemContainer.getLastCartItems() != null) {
                        data.putAll(RecommendationLogic.cart(lastTrackedItemContainer.getLastCartItems()).getData().getLogicData());
                    }
                    break;
                case RecommendationLogic.CATEGORY:
                    if (lastTrackedItemContainer.getLastCategoryPath() != null) {
                        data.putAll(RecommendationLogic.category(lastTrackedItemContainer.getLastCategoryPath()).getData().getLogicData());
                    }
                    break;
                case RecommendationLogic.POPULAR:
                    if (lastTrackedItemContainer.getLastCategoryPath() != null) {
                        data.putAll(RecommendationLogic.popular(lastTrackedItemContainer.getLastCategoryPath()).getData().getLogicData());
                    }
                    break;
                case RecommendationLogic.RELATED:
                    if (lastTrackedItemContainer.getLastItemView() != null) {
                        data.putAll(RecommendationLogic.related(lastTrackedItemContainer.getLastItemView()).getData().getLogicData());
                    }
                    break;
                case RecommendationLogic.ALSO_BOUGHT:
                    if (lastTrackedItemContainer.getLastItemView() != null) {
                        data.putAll(RecommendationLogic.alsoBought(lastTrackedItemContainer.getLastItemView()).getData().getLogicData());
                    }
                    break;
            }
        }
        for (String key : data.keySet()) {
            uriBuilder.appendQueryParameter(key, data.get(key));
        }

        return uriBuilder.build().toString();
    }

    private String createPersonalRecommendationUrl(Logic logic) {
        LogicData logicData = logic.getData();

        if (logicData.getExtensions().isEmpty()) {
            uriBuilder.appendQueryParameter("f", "f:" + logic.getLogicName() + ",l:" + limit + ",o:0");
        } else {
            List<String> params = new ArrayList<>();
            for (String extension : logicData.getExtensions()) {
                params.add("f:" + logic.getLogicName() + "_" + extension + ",l:" + limit + ",o:0");
            }
            uriBuilder.appendQueryParameter("f", TextUtils.join("|", params));
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

    private String createRecommendationFilterQueryValues() {
        List recommendationFilterQueryValues = new ArrayList<>();
        for (RecommendationFilter filter : this.filters) {
            Map<String, Object> recommendationFilterQueryValue = new LinkedHashMap<>();
            recommendationFilterQueryValue.put("f", filter.getField());
            recommendationFilterQueryValue.put("r", filter.getComparison());
            recommendationFilterQueryValue.put("v", createRecommendationFilterValueStringRepresentation(filter.getExpectations()));
            recommendationFilterQueryValue.put("n", !filter.getType().equals("EXCLUDE"));
            recommendationFilterQueryValues.add(recommendationFilterQueryValue);
        }
        return JsonUtils.fromList(recommendationFilterQueryValues).toString();
    }

    private String createRecommendationFilterValueStringRepresentation(List<String> expectations) {
        StringBuilder valuesStringRepresentation = new StringBuilder();
        for (int i = 0; i < expectations.size(); i++) {
            if (i > 0) {
                valuesStringRepresentation.append("|");
            }
            valuesStringRepresentation.append(expectations.get(i));
        }
        return valuesStringRepresentation.toString();
    }

}
