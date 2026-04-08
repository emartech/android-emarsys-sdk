package com.emarsys.predict.util

import com.emarsys.predict.api.model.CartItem
import java.net.URLEncoder
import kotlin.text.Charsets.UTF_8

object CartItemUtils {
    fun cartItemsToQueryParam(items: List<CartItem>): String {
        val sb = StringBuilder()

        for (i in items.indices) {
            if (i != 0) {
                sb.append("|")
            }
            sb.append(cartItemToQueryParam(items[i]))
        }

        return sb.toString()
    }

    private fun cartItemToQueryParam(cartItem: CartItem): String {
        return "i:" + URLEncoder.encode(
            cartItem.itemId,
            UTF_8
        ) + ",p:" + cartItem.price + ",q:" + cartItem.quantity
    }
}
