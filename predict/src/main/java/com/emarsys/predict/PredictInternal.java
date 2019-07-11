package com.emarsys.predict;

import com.emarsys.predict.api.model.CartItem;

import java.util.List;

public interface PredictInternal {
    void setContact(String contactId);

    void clearContact();

    String trackCart(List<CartItem> items);

    String trackPurchase(String orderId, List<CartItem> items);

    String trackItemView(String itemId);

    String trackCategoryView(String categoryPath);

    String trackSearchTerm(String searchTerm);
}
