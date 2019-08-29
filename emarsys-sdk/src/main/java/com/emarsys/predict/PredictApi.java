package com.emarsys.predict;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.predict.api.model.CartItem;
import com.emarsys.predict.api.model.Logic;
import com.emarsys.predict.api.model.Product;
import com.emarsys.predict.api.model.RecommendationFilter;

import java.util.List;
import java.util.Map;

public interface PredictApi {
    void trackCart(@NonNull final List<CartItem> items);

    void trackPurchase(@NonNull final String orderId,
                       @NonNull final List<CartItem> items);

    void trackItemView(@NonNull final String itemId);

    void trackItemView(@NonNull final Product product);

    void trackCategoryView(@NonNull final String categoryPath);

    void trackSearchTerm(@NonNull final String searchTerm);

    void trackTag(@NonNull final String tag, @Nullable final Map<String, String> attributes);

    void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final ResultListener<Try<List<Product>>> resultListener);

    void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final Integer limit, @NonNull final ResultListener<Try<List<Product>>> resultListener);

    void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final List<RecommendationFilter> recommendationFilters, @NonNull final ResultListener<Try<List<Product>>> resultListener);

    void recommendProducts(@NonNull final Logic recommendationLogic, @NonNull final Integer limit, @NonNull final List<RecommendationFilter> recommendationFilters, @NonNull final ResultListener<Try<List<Product>>> resultListener);

}
