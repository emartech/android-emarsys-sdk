package com.emarsys.predict;

import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.util.SystemUtils;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.MethodNotAllowed;
import com.emarsys.predict.api.model.CartItem;
import com.emarsys.predict.api.model.Logic;
import com.emarsys.predict.api.model.Product;
import com.emarsys.predict.api.model.RecommendationFilter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggingPredictInternal implements PredictInternal {


    private final Class klass;

    public LoggingPredictInternal(Class klass) {
        this.klass = klass;
    }

    @Override
    public void setContact(String contactId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("contact_id", contactId);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(klass, callerMethodName, parameters));
    }

    @Override
    public void clearContact() {
        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(klass, callerMethodName, null));
    }

    @Override
    public String trackCart(List<CartItem> items) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("items", items.toString());

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(klass, callerMethodName, parameters));
        return null;
    }

    @Override
    public String trackPurchase(String orderId, List<CartItem> items) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("order_id", orderId);
        parameters.put("items", items.toString());

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(klass, callerMethodName, parameters));
        return null;
    }

    @Override
    public String trackItemView(String itemId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("item_id", itemId);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(klass, callerMethodName, parameters));
        return null;
    }

    @Override
    public String trackItemView(Product product) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("product", product.toString());

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(klass, callerMethodName, parameters));
        return null;
    }

    @Override
    public String trackCategoryView(String categoryPath) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("category_path", categoryPath);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(klass, callerMethodName, parameters));
        return null;
    }

    @Override
    public String trackSearchTerm(String searchTerm) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("search_term", searchTerm);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(klass, callerMethodName, parameters));
        return null;
    }

    @Override
    public void recommendProducts(Logic recommendationLogic, Integer limit, List<RecommendationFilter> recommendationFilter, ResultListener<Try<List<Product>>> resultListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("recommendation_logic", recommendationLogic.toString());
        parameters.put("result_listener", resultListener != null);
        parameters.put("limit", limit);
        parameters.put("recommendation_filter", Arrays.toString(recommendationFilter.toArray()));

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(klass, callerMethodName, parameters));
    }
}
