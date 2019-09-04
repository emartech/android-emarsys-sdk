package com.emarsys.sample

import com.emarsys.predict.api.model.CartItem

data class SampleCartItem(
        private var price: Double,
        private var quantity: Double,
        private var itemId: String) : CartItem {

    override fun getPrice(): Double {
        return price
    }

    override fun getItemId(): String {
        return itemId
    }

    override fun getQuantity(): Double {
        return quantity
    }
}