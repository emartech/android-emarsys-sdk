package com.emarsys.predict;

import androidx.annotation.NonNull;

import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.predict.api.model.CartItem;
import com.emarsys.predict.api.model.Logic;
import com.emarsys.predict.api.model.Product;
import com.emarsys.predict.api.model.RecommendationFilter;

import java.util.List;

public interface PredictApi {
    void trackCart(@NonNull final List<CartItem> items);

    void trackPurchase(@NonNull final String orderId,
                       @NonNull final List<CartItem> items);

    void trackItemView(@NonNull final String itemId);

    void trackCategoryView(@NonNull final String categoryPath);

    void trackSearchTerm(@NonNull final String searchTerm);

    void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final ResultListener<Try<List<Product>>> resultListener);

    void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final Integer limit, @NonNull final ResultListener<Try<List<Product>>> resultListener);

    void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final List<RecommendationFilter> recommendationFilters, @NonNull final ResultListener<Try<List<Product>>> resultListener);

    void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final Integer limit, @NonNull final List<RecommendationFilter> recommendationFilters, @NonNull final ResultListener<Try<List<Product>>> resultListener);
}
