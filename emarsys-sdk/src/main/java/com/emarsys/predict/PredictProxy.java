package com.emarsys.predict;


import androidx.annotation.NonNull;

import com.emarsys.core.RunnerProxy;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.util.Assert;
import com.emarsys.predict.api.model.CartItem;
import com.emarsys.predict.api.model.Product;

import java.util.List;

public class PredictProxy implements PredictApi {

    private final RunnerProxy runnerProxy;
    private final PredictInternal predictInternal;

    public PredictProxy(RunnerProxy runnerProxy, PredictInternal predictInternal) {
        Assert.notNull(runnerProxy, "RunnerProxy must not be null!");
        Assert.notNull(predictInternal, "PredictInternal must not be null!");

        this.runnerProxy = runnerProxy;
        this.predictInternal = predictInternal;
    }

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

    public void trackItemView(@NonNull final String itemId) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(itemId, "ItemId must not be null!");

                predictInternal.trackItemView(itemId);
            }
        });
    }

    public void trackCategoryView(@NonNull final String categoryPath) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(categoryPath, "CategoryPath must not be null!");

                predictInternal.trackCategoryView(categoryPath);
            }
        });
    }

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
    public void recommendProducts(@NonNull final ResultListener<Try<List<Product>>> resultListener) {
        runnerProxy.logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(resultListener, "ResultListener must not be null!");

                predictInternal.recommendProducts(resultListener);
            }
        });
    }
}