package com.emarsys.predict.model;

import com.emarsys.predict.api.model.CartItem;

import java.util.List;

public class LastTrackedItemContainer {

    private List<CartItem> lastCartItems;
    private String lastItemView;
    private String lastCategoryPath;
    private String lastSearchTerm;

    public List<CartItem> getLastCartItems() {
        return lastCartItems;
    }

    public void setLastCartItems(List<CartItem> lastCartItems) {
        this.lastCartItems = lastCartItems;
    }

    public String getLastItemView() {
        return lastItemView;
    }

    public void setLastItemView(String lastItemView) {
        this.lastItemView = lastItemView;
    }

    public String getLastCategoryPath() {
        return lastCategoryPath;
    }

    public void setLastCategoryPath(String lastCategoryPath) {
        this.lastCategoryPath = lastCategoryPath;
    }

    public String getLastSearchTerm() {
        return lastSearchTerm;
    }

    public void setLastSearchTerm(String lastSearchTerm) {
        this.lastSearchTerm = lastSearchTerm;
    }
}
