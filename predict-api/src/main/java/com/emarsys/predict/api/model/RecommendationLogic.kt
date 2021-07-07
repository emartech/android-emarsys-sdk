package com.emarsys.predict.api.model

import java.lang.StringBuilder

class RecommendationLogic @JvmOverloads internal constructor(override val logicName: String, override val data: Map<String, String>? = mapOf(), override val variants: List<String>? = listOf()) : Logic {

    companion object {
        const val SEARCH = "SEARCH"
        const val CART = "CART"
        const val RELATED = "RELATED"
        const val CATEGORY = "CATEGORY"
        const val ALSO_BOUGHT = "ALSO_BOUGHT"
        const val POPULAR = "POPULAR"
        const val PERSONAL = "PERSONAL"
        const val HOME = "HOME"

        @JvmStatic
        fun search(): Logic {
            return RecommendationLogic(SEARCH)
        }

        @JvmStatic
        fun search(searchTerm: String): Logic {
            val data: Map<String, String> = mapOf(
                    "q" to searchTerm
            )
            return RecommendationLogic(SEARCH, data)
        }

        @JvmStatic
        fun cart(): Logic {
            return RecommendationLogic(CART)
        }

        @JvmStatic
        fun cart(cartItems: List<CartItem>): Logic {
            val data = mapOf(
                    "cv" to "1",
                    "ca" to cartItemsToQueryParam(cartItems)
            )

            return RecommendationLogic(CART, data)
        }

        @JvmStatic
        fun related(): Logic {
            return RecommendationLogic(RELATED)
        }

        @JvmStatic
        fun related(itemId: String): Logic {
            val data = mapOf("v" to "i:$itemId")

            return RecommendationLogic(RELATED, data)
        }

        @JvmStatic
        fun category(): Logic {
            return RecommendationLogic(CATEGORY)
        }

        @JvmStatic
        fun category(categoryPath: String): Logic {
            val data = mapOf("vc" to categoryPath)

            return RecommendationLogic(CATEGORY, data)
        }

        @JvmStatic
        fun alsoBought(): Logic {
            return RecommendationLogic(ALSO_BOUGHT)
        }

        @JvmStatic
        fun alsoBought(itemId: String): Logic {
            val data = mapOf("v" to "i:$itemId")
            return RecommendationLogic(ALSO_BOUGHT, data)
        }

        @JvmStatic
        fun popular(): Logic {
            return RecommendationLogic(POPULAR)
        }

        @JvmStatic
        fun popular(categoryPath: String): Logic {
            val data = mapOf("vc" to categoryPath)

            return RecommendationLogic(POPULAR, data)
        }

        @JvmStatic
        @JvmOverloads
        fun personal(variants: List<String>? = listOf()): Logic {
            return RecommendationLogic(PERSONAL, mapOf(), variants)
        }

        @JvmStatic
        @JvmOverloads
        fun home(variants: List<String>? = listOf()): Logic {
            return RecommendationLogic(HOME, mapOf(), variants)
        }

        private fun cartItemsToQueryParam(items: List<CartItem>): String {
            val sb = StringBuilder()
            for (i in items.indices) {
                if (i != 0) {
                    sb.append("|")
                }
                sb.append(cartItemToQueryParam(items[i]))
            }
            return sb.toString()
        }

        private fun cartItemToQueryParam(cartItem: CartItem?): String {
            return "i:" + cartItem!!.itemId + ",p:" + cartItem.price + ",q:" + cartItem.quantity
        }
    }
}