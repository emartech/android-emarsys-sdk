package com.emarsys.predict.util;

import com.emarsys.core.util.Assert;
import com.emarsys.predict.api.model.CartItem;

import java.net.URLEncoder;
import java.util.List;

import kotlin.text.Charsets;

public class CartItemUtils {

    public static String cartItemsToQueryParam(List<CartItem> items) {
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
        return "i:" + URLEncoder.encode(cartItem.getItemId(), Charsets.UTF_8) + ",p:" + cartItem.getPrice() + ",q:" + cartItem.getQuantity();
    }

}
