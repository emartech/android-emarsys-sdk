package com.emarsys.sample

import com.emarsys.predict.api.model.CartItem

data class SampleCartItem(override val itemId: String,
                          override val price: Double,
                          override val quantity: Double) : CartItem