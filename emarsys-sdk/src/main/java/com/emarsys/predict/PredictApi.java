package com.emarsys.predict;

import androidx.annotation.NonNull;

import com.emarsys.predict.api.model.CartItem;

import java.util.List;

public interface PredictApi {
    void trackCart(@NonNull final List<CartItem> items);

    void trackPurchase(@NonNull final String orderId,
                       @NonNull final List<CartItem> items);

    void trackItemView(@NonNull final String itemId);

    void trackCategoryView(@NonNull final String categoryPath);

    void trackSearchTerm(@NonNull final String searchTerm);
}
