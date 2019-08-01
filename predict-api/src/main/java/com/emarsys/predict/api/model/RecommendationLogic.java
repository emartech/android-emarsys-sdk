package com.emarsys.predict.api.model;

import androidx.annotation.NonNull;

import com.emarsys.core.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RecommendationLogic implements Logic {

    private String logicName;
    private Map<String, String> data;

    private RecommendationLogic(String logicName, Map<String, String> data) {
        this.logicName = logicName;
        this.data = data;
    }

    public static Logic search() {
        Map<String, String> data = new HashMap<>();
        return new RecommendationLogic("SEARCH", data);
    }

    public static Logic search(@NonNull String searchTerm) {
        Assert.notNull(searchTerm, "SearchTerm must not be null!");

        Map<String, String> data = new HashMap<>();
        data.put("q", searchTerm);
        return new RecommendationLogic("SEARCH", data);
    }

    public static Logic cart() {
        Map<String, String> data = new HashMap<>();
        return new RecommendationLogic("CART", data);
    }

    public static Logic cart(@NonNull List<CartItem> cartItems) {
        Assert.notNull(cartItems, "CartItems must not be null!");

        Map<String, String> data = new HashMap<>();
        data.put("cv", "1");
        data.put("ca", cartItemsToQueryParam(cartItems));
        return new RecommendationLogic("CART", data);
    }

    public static Logic related() {
        Map<String, String> data = new HashMap<>();
        return new RecommendationLogic("RELATED", data);
    }

    public static Logic related(String itemId) {
        Assert.notNull(itemId, "ItemId must not be null!");
        Map<String, String> data = new HashMap<>();
        data.put("v", String.format("i:%s", itemId));
        return new RecommendationLogic("RELATED", data);
    }

    public static Logic category() {
        Map<String, String> data = new HashMap<>();
        return new RecommendationLogic("CATEGORY", data);
    }

    public static Logic category(String categoryPath) {
        Assert.notNull(categoryPath, "CategoryPath must not be null!");
        Map<String, String> data = new HashMap<>();
        data.put("vc", categoryPath);
        return new RecommendationLogic("CATEGORY", data);
    }

    public static Logic alsoBought() {
        Map<String, String> data = new HashMap<>();
        return new RecommendationLogic("ALSO_BOUGHT", data);
    }

    public static Logic alsoBought(String itemId) {
        Assert.notNull(itemId, "ItemId must not be null!");
        Map<String, String> data = new HashMap<>();
        data.put("v", String.format("i:%s", itemId));
        return new RecommendationLogic("ALSO_BOUGHT", data);
    }

    public static Logic popular() {
        Map<String, String> data = new HashMap<>();
        return new RecommendationLogic("POPULAR", data);
    }

    public static Logic popular(String categoryPath) {
        Assert.notNull(categoryPath, "CategoryPath must not be null!");
        Map<String, String> data = new HashMap<>();
        data.put("vc", categoryPath);
        return new RecommendationLogic("POPULAR", data);
    }

    @Override
    public String getLogicName() {
        return logicName;
    }

    @Override
    public Map<String, String> getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecommendationLogic that = (RecommendationLogic) o;
        return Objects.equals(logicName, that.logicName) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logicName, data);
    }

    @Override
    public String toString() {
        return "RecommendationLogic{" +
                "logicName='" + logicName + '\'' +
                ", data=" + data +
                '}';
    }

    private static String cartItemsToQueryParam(List<CartItem> items) {
        Assert.notNull(items, "Items must not be null!");
        Assert.elementsNotNull(items, "Item elements must not be null!");

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < items.size(); ++i) {
            if (i != 0) {
                sb.append("|");
            }
            sb.append(cartItemToQueryParam(items.get(i)));
        }

        return sb.toString();
    }

    private static String cartItemToQueryParam(CartItem cartItem) {
        return "i:" + cartItem.getItemId() + ",p:" + cartItem.getPrice() + ",q:" + cartItem.getQuantity();
    }
}