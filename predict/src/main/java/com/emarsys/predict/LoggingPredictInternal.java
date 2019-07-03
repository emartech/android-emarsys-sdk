package com.emarsys.predict;

import com.emarsys.predict.api.model.CartItem;

import java.util.List;

public class LoggingPredictInternal implements PredictInternal {


    @Override
    public void setContact(String contactId) {

    }

    @Override
    public void clearContact() {

    }

    @Override
    public String trackCart(List<CartItem> items) {
        return null;
    }

    @Override
    public String trackPurchase(String orderId, List<CartItem> items) {
        return null;
    }

    @Override
    public String trackItemView(String itemId) {
        return null;
    }

    @Override
    public String trackCategoryView(String categoryPath) {
        return null;
    }

    @Override
    public String trackSearchTerm(String searchTerm) {
        return null;
    }
}
