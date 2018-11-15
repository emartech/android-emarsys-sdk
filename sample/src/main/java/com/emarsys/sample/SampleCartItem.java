package com.emarsys.sample;

import com.emarsys.predict.api.model.CartItem;

public class SampleCartItem implements CartItem {
    private double price;
    private double quantity;
    private String itemId;

    SampleCartItem(String itemId, double price, double quantity) {
        this.itemId = itemId;
        this.price = price;
        this.quantity = quantity;
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public double getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "CartItem{itemId:" + itemId + ", price:" + price + ", quantity:" + quantity + "}";
    }

}