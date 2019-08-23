package com.emarsys.predict

import com.emarsys.core.response.ResponseModel
import com.emarsys.predict.api.model.Product
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class PredictResponseMapperTest {

    private lateinit var mockResponseModel: ResponseModel

    private lateinit var expectedResult: List<Product>

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun init() {
        mockResponseModel = Mockito.mock(ResponseModel::class.java)
    }

    private fun getExpectedResult(feature: String): List<Product> {
        return listOf(
                Product.Builder(
                        "2119",
                        "LSL Men Polo Shirt SE16",
                        "http://lifestylelabels.com/lsl-men-polo-shirt-se16.html",
                        feature)
                        .categoryPath("MEN>Shirts")
                        .available(true)
                        .msrp(100.0F)
                        .price(100.0F)
                        .imageUrl("http://lifestylelabels.com/pub/media/catalog/product/m/p/mp001.jpg")
                        .zoomImageUrl("http://lifestylelabels.com/pub/media/catalog/product/m/p/mp001.jpg")
                        .productDescription("product Description")
                        .album("album")
                        .actor("actor")
                        .artist("artist")
                        .author("author")
                        .brand("brand")
                        .year(2000)
                        .customFields(hashMapOf(
                                "msrp_gpb" to "83.2",
                                "price_gpb" to "83.2",
                                "msrp_aed" to "100",
                                "price_aed" to "100",
                                "msrp_cad" to "100",
                                "price_cad" to "100",
                                "msrp_mxn" to "2057.44",
                                "price_mxn" to "2057.44",
                                "msrp_pln" to "100",
                                "price_pln" to "100",
                                "msrp_rub" to "100",
                                "price_rub" to "100",
                                "msrp_sek" to "100",
                                "price_sek" to "100",
                                "msrp_try" to "339.95",
                                "price_try" to "339.95",
                                "msrp_usd" to "100",
                                "price_usd" to "100"
                        ))
                        .build(),
                Product.Builder(
                        "2120",
                        "LSL Men Polo Shirt SE16",
                        "http://lifestylelabels.com/lsl-men-polo-shirt-se16.html",
                        feature)
                        .build())
    }

    @Test
    fun testMap_withSearch_shouldPreserveOrder() {
        whenever(mockResponseModel.body).thenReturn(getBodyFor("SEARCH"))
        val predictResponseMapper = PredictResponseMapper()
        val result = predictResponseMapper.map(mockResponseModel)
        expectedResult = getExpectedResult("SEARCH")
        result shouldContainAll expectedResult

        result.count() shouldBe 2
        result[0] shouldBe expectedResult[0]
        result[1] shouldBe expectedResult[1]
    }

    @Test
    fun testMap_withCart_shouldPreserveOrder() {
        whenever(mockResponseModel.body).thenReturn(getBodyFor("CART"))
        val predictResponseMapper = PredictResponseMapper()
        val result = predictResponseMapper.map(mockResponseModel)
        expectedResult = getExpectedResult("CART")

        result shouldContainAll expectedResult

        result.count() shouldBe 2
        result[0] shouldBe expectedResult[0]
        result[1] shouldBe expectedResult[1]
    }

    @Test
    fun testMap_withRelated_shouldPreserveOrder() {
        whenever(mockResponseModel.body).thenReturn(getBodyFor("RELATED"))
        val predictResponseMapper = PredictResponseMapper()
        val result = predictResponseMapper.map(mockResponseModel)
        expectedResult = getExpectedResult("RELATED")

        result shouldContainAll expectedResult

        result.count() shouldBe 2
        result[0] shouldBe expectedResult[0]
        result[1] shouldBe expectedResult[1]
    }

    private fun getBodyFor(feature: String): String {
        return """{
           "cohort":"AAAA",
           "visitor":"16BCC0D2745E6B36",
           "session":"24E844D1E58C1C2",
           "features":{
              "$feature":{
                 "hasMore":true,
                 "merchants":[
                    "1428C8EE286EC34B"
                 ],
                 "items":[
                        {"id":"2119"},
                        {"id":"2120"}
                 ]
              }
           },
           "products":{
              "2119":{
                 "item":"2119",
                 "category":"MEN>Shirts",
                 "title":"LSL Men Polo Shirt SE16",
                 "available":true,
                 "msrp":100.0,
                 "price":100.0,
                 "msrp_gpb":"83.2",
                 "price_gpb":"83.2",
                 "msrp_aed":"100",
                 "price_aed":"100",
                 "msrp_cad":"100",
                 "price_cad":"100",
                 "msrp_mxn":"2057.44",
                 "price_mxn":"2057.44",
                 "msrp_pln":"100",
                 "price_pln":"100",
                 "msrp_rub":"100",
                 "price_rub":"100",
                 "msrp_sek":"100",
                 "price_sek":"100",
                 "msrp_try":"339.95",
                 "price_try":"339.95",
                 "msrp_usd":"100",
                 "price_usd":"100",
                 "link":"http://lifestylelabels.com/lsl-men-polo-shirt-se16.html",
                 "image":"http://lifestylelabels.com/pub/media/catalog/product/m/p/mp001.jpg",
                 "zoom_image":"http://lifestylelabels.com/pub/media/catalog/product/m/p/mp001.jpg",
                 "description":"product Description",
                 "album":"album",
                 "actor":"actor",
                 "artist":"artist",
                 "author":"author",
                 "brand":"brand",
                 "year":"2000"
              },
              "2120":{
                 "item":"2120",
                 "title":"LSL Men Polo Shirt SE16",
                 "link":"http://lifestylelabels.com/lsl-men-polo-shirt-se16.html"
              }
           }
        }"""
    }
}

