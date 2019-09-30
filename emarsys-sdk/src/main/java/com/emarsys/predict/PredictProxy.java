package com.emarsys.predict;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emarsys.core.RunnerProxy;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.util.Assert;
import com.emarsys.predict.api.model.CartItem;
import com.emarsys.predict.api.model.Logic;
import com.emarsys.predict.api.model.Product;
import com.emarsys.predict.api.model.RecommendationFilter;

import java.util.List;
import java.util.Map;

public class PredictProxy implements PredictApi {

    private final RunnerProxy runnerProxy;
    private final PredictInternal predictInternal;

    public PredictProxy(RunnerProxy runnerProxy, PredictInternal predictInternal) {
        Assert.notNull(runnerProxy, "RunnerProxy must not be null!");
        Assert.notNull(predictInternal, "PredictInternal must not be null!");

        this.runnerProxy = runnerProxy;
        this.predictInternal = predictInternal;
    }

    @Override
    public void trackCart(@NonNull final List<CartItem> items) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(items, "Items must not be null!");
                Assert.elementsNotNull(items, "Item elements must not be null!");

                predictInternal.trackCart(items);
            }
        });
    }

    @Override
    public void trackPurchase(@NonNull final String orderId,
                              @NonNull final List<CartItem> items) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(orderId, "OrderId must not be null!");
                Assert.notNull(items, "Items must not be null!");
                Assert.elementsNotNull(items, "Item elements must not be null!");

                predictInternal.trackPurchase(orderId, items);
            }
        });
    }

    @Override
    public void trackItemView(@NonNull final String itemId) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(itemId, "ItemId must not be null!");

                predictInternal.trackItemView(itemId);
            }
        });
    }

    @Override
    public void trackCategoryView(@NonNull final String categoryPath) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(categoryPath, "CategoryPath must not be null!");

                predictInternal.trackCategoryView(categoryPath);
            }
        });
    }

    @Override
    public void trackSearchTerm(@NonNull final String searchTerm) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(searchTerm, "SearchTerm must not be null!");

                predictInternal.trackSearchTerm(searchTerm);
            }
        });
    }

    @Override
    public void trackTag(@NonNull final String tag, @Nullable final Map<String, String> attributes) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(tag, "Tag must not be null!");

                predictInternal.trackTag(tag, attributes);
            }
        });
    }

    @Override
    public void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final ResultListener<Try<List<Product>>> resultListener) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(recommendationLogic, "RecommendationLogic must not be null!");
                Assert.notNull(resultListener, "ResultListener must not be null!");

                predictInternal.recommendProducts(recommendationLogic, null, null, resultListener);
            }
        });
    }

    @Override
    public void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final Integer limit, @NonNull final ResultListener<Try<List<Product>>> resultListener) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(recommendationLogic, "RecommendationLogic must not be null!");
                Assert.notNull(resultListener, "ResultListener must not be null!");
                Assert.notNull(limit, "Limit must not be null!");
                Assert.positiveInt(limit, "Limit must be greater than zero!");

                predictInternal.recommendProducts(recommendationLogic, limit, null, resultListener);
            }
        });
    }

    @Override
    public void recommendProducts(@NonNull final Logic recommendationLogic,
                                  @NonNull final List<RecommendationFilter> recommendationFilters,
                                  @NonNull final ResultListener<Try<List<Product>>> resultListener) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(recommendationLogic, "RecommendationLogic must not be null!");
                Assert.notNull(resultListener, "ResultListener must not be null!");
                Assert.notNull(recommendationFilters, "RecommendationFilters must not be null!");

                predictInternal.recommendProducts(recommendationLogic, null, recommendationFilters, resultListener);

            }
        });
    }

    @Override
    public void recommendProducts(@NonNull final Logic recommendationLogic,
                                  @NonNull final List<RecommendationFilter> recommendationFilters,
                                  @NonNull final Integer limit,
                                  @NonNull final ResultListener<Try<List<Product>>> resultListener) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(recommendationLogic, "RecommendationLogic must not be null!");
                Assert.notNull(resultListener, "ResultListener must not be null!");
                Assert.notNull(limit, "Limit must not be null!");
                Assert.positiveInt(limit, "Limit must be greater than zero!");
                Assert.notNull(recommendationFilters, "RecommendationFilter must not be null!");

                predictInternal.recommendProducts(recommendationLogic, limit, recommendationFilters, resultListener);
            }
        });
    }

    @Override
    public void trackRecommendationClick(@NonNull final Product product) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(product, "Product must not be null!");

                predictInternal.trackRecommendationClick(product);
            }
        });
    }
}