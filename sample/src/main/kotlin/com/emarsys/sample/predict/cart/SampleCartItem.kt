package com.emarsys.sample.predict.cart

import com.emarsys.predict.api.model.CartItem

data class SampleCartItem(
    override val itemId: String,
    override val price: Double,
    override val quantity: Double
) : CartItem {

    override fun toString(): String {
        return "Sample Cart Item: id = $itemId"
    }
}