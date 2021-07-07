package com.emarsys.predict.api.model


data class PredictCartItem(override val itemId: String, override val price: Double, override val quantity: Double) : CartItem