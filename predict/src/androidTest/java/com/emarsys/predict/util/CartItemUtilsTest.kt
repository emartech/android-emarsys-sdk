package com.emarsys.predict.util

import com.emarsys.predict.api.model.CartItem
import com.emarsys.predict.api.model.PredictCartItem
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class CartItemUtilsTest {

    lateinit var cartItem1: CartItem
    lateinit var cartItem2: CartItem
    lateinit var cartItem3: CartItem

    @Before
    fun init() {
        cartItem1 = PredictCartItem("1", 100.0, 2.0)
        cartItem2 = PredictCartItem("2", 200.0, 4.0)
        cartItem3 = PredictCartItem("3", 300.0, 8.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCartItemsToQueryParam_cartItems_mustNotBeNull() {
        CartItemUtils.cartItemsToQueryParam(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCartItemsToQueryParam_cartItems_mustNotContainNullElements() {
        CartItemUtils.cartItemsToQueryParam(listOf(
                mock(CartItem::class.java),
                null,
                mock(CartItem::class.java)
        ))
    }

    @Test
    fun testCartItemsToQueryParam_emptyList() {
        assertEquals("", CartItemUtils.cartItemsToQueryParam(listOf()))
    }

    @Test
    fun testCartItemsToQueryParam_singletonList() {
        assertEquals(
                "i:1,p:100.0,q:2.0",
                CartItemUtils.cartItemsToQueryParam(listOf(cartItem1)))
    }

    @Test
    fun testCartItemsToQueryParam() {
        assertEquals(
                "i:1,p:100.0,q:2.0|i:2,p:200.0,q:4.0|i:3,p:300.0,q:8.0",
                CartItemUtils.cartItemsToQueryParam(listOf(cartItem1, cartItem2, cartItem3)))
    }

}