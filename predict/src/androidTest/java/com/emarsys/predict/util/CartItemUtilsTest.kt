package com.emarsys.predict.util

import com.emarsys.predict.api.model.CartItem
import com.emarsys.predict.api.model.PredictCartItem
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

import org.mockito.Mockito.mock

class CartItemUtilsTest {


    private lateinit var cartItem1: CartItem
    private lateinit var cartItem2: CartItem
    private lateinit var cartItem3: CartItem

    @BeforeEach
    fun init() {
        cartItem1 = PredictCartItem("1", 100.0, 2.0)
        cartItem2 = PredictCartItem("2", 200.0, 4.0)
        cartItem3 = PredictCartItem("3", 300.0, 8.0)
    }

    @Test
    fun testCartItemsToQueryParam_cartItems_mustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            CartItemUtils.cartItemsToQueryParam(null)
        }
    }

    @Test
    fun testCartItemsToQueryParam_cartItems_mustNotContainNullElements() {
        shouldThrow<IllegalArgumentException> {
            CartItemUtils.cartItemsToQueryParam(
                listOf(
                    mock(CartItem::class.java),
                    null,
                    mock(CartItem::class.java)
                )
            )
        }
    }

    @Test
    fun testCartItemsToQueryParam_emptyList() {
        CartItemUtils.cartItemsToQueryParam(listOf()) shouldBe ""
    }

    @Test
    fun testCartItemsToQueryParam_singletonList() {
        CartItemUtils.cartItemsToQueryParam(listOf(cartItem1)) shouldBe "i:1,p:100.0,q:2.0"
    }

    @Test
    fun testCartItemsToQueryParam() {
        CartItemUtils.cartItemsToQueryParam(
            listOf(
                cartItem1,
                cartItem2,
                cartItem3
            )
        ) shouldBe "i:1,p:100.0,q:2.0|i:2,p:200.0,q:4.0|i:3,p:300.0,q:8.0"
    }
}