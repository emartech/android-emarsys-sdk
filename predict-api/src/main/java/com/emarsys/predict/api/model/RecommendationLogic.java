package com.emarsys.predict.api.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emarsys.core.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RecommendationLogic implements Logic {

    public static final String SEARCH = "SEARCH";
    public static final String CART = "CART";
    public static final String RELATED = "RELATED";
    public static final String CATEGORY = "CATEGORY";
    public static final String ALSO_BOUGHT = "ALSO_BOUGHT";
    public static final String POPULAR = "POPULAR";
    public static final String PERSONAL = "PERSONAL";

    private String logicName;
    private Map<String, String> data;
    private List<String> variants;



    RecommendationLogic(String logicName, Map<String, String> logicData, List<String> variants) {
        Assert.notNull(logicName, "LogicName must not be null!");
        Assert.notNull(logicData, "LogicData must not be null!");
        Assert.notNull(variants, "Variants must not be null!");

        this.logicName = logicName;
        this.data = logicData;
        this.variants = variants;
    }

    RecommendationLogic(String logicName, Map<String, String> logicData) {
        this(logicName, logicData, new ArrayList<String>());
    }

    RecommendationLogic(String logicName) {
        this(logicName, new HashMap<String, String>(), new ArrayList<String>());
    }

    public static Logic search() {
        return new RecommendationLogic(SEARCH);
    }

    public static Logic search(@NonNull String searchTerm) {
        Assert.notNull(searchTerm, "SearchTerm must not be null!");

        Map<String, String> data = new HashMap<>();
        data.put("q", searchTerm);
        return new RecommendationLogic(SEARCH, data);
    }

    public static Logic cart() {
        return new RecommendationLogic(CART);
    }

    public static Logic cart(@NonNull List<CartItem> cartItems) {
        Assert.notNull(cartItems, "CartItems must not be null!");

        Map<String, String> data = new HashMap<>();
        data.put("cv", "1");
        data.put("ca", cartItemsToQueryParam(cartItems));
        return new RecommendationLogic(CART, data);
    }

    public static Logic related() {
        return new RecommendationLogic(RELATED);
    }

    public static Logic related(@NonNull String itemId) {
        Assert.notNull(itemId, "ItemId must not be null!");
        Map<String, String> data = new HashMap<>();
        data.put("v", String.format("i:%s", itemId));
        return new RecommendationLogic(RELATED, data);
    }

    public static Logic category() {
        return new RecommendationLogic(CATEGORY);
    }

    public static Logic category(@NonNull String categoryPath) {
        Assert.notNull(categoryPath, "CategoryPath must not be null!");
        Map<String, String> data = new HashMap<>();
        data.put("vc", categoryPath);
        return new RecommendationLogic(CATEGORY, data);
    }

    public static Logic alsoBought() {
        return new RecommendationLogic(ALSO_BOUGHT);
    }

    public static Logic alsoBought(@NonNull String itemId) {
        Assert.notNull(itemId, "ItemId must not be null!");
        Map<String, String> data = new HashMap<>();
        data.put("v", String.format("i:%s", itemId));
        return new RecommendationLogic(ALSO_BOUGHT, data);
    }

    public static Logic popular() {
        return new RecommendationLogic(POPULAR);
    }

    public static Logic popular(@NonNull String categoryPath) {
        Assert.notNull(categoryPath, "CategoryPath must not be null!");
        Map<String, String> data = new HashMap<>();
        data.put("vc", categoryPath);
        return new RecommendationLogic(POPULAR, data);
    }

    public static Logic personal() {
        return personal(new ArrayList<String>());
    }

    public static Logic personal(@Nullable List<String> variants) {
        Assert.notNull(variants, "Variants must not be null!");
        Map<String, String> data = new HashMap<>();

        return new RecommendationLogic(PERSONAL, data, variants);
    }

    @Override
    public String getLogicName() {
        return logicName;
    }

    @Override
    public List<String> getVariants() {
        return variants;
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
                Objects.equals(data, that.data) &&
                Objects.equals(variants, that.variants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logicName, data, variants);
    }

    @Override
    public String toString() {
        return "RecommendationLogic{" +
                "logicName='" + logicName + '\'' +
                "variants='" + variants + '\'' +
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