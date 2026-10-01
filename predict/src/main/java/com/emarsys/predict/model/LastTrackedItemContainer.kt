package com.emarsys.predict.model

import com.emarsys.predict.api.model.CartItem

class LastTrackedItemContainer {
    var lastCartItems: List<CartItem>? = null
    var lastItemView: String? = null
    var lastCategoryPath: String? = null
    var lastSearchTerm: String? = null
}
