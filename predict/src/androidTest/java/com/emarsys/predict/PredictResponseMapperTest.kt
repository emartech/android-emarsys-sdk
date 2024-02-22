package com.emarsys.predict

import com.emarsys.core.response.ResponseModel
import com.emarsys.predict.api.model.Product
import com.emarsys.testUtil.mockito.whenever
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

import org.mockito.Mockito

class PredictResponseMapperTest {

    private lateinit var predictResponseMapper: PredictResponseMapper
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var expectedResult: List<Product>


    @BeforeEach
    fun setUp() {
        predictResponseMapper = PredictResponseMapper()
        mockResponseModel = Mockito.mock(ResponseModel::class.java)
    }

    @Test
    fun testMap_withSearch_shouldPreserveOrder() {
        whenever(mockResponseModel.body).thenReturn(getBodyFor("SEARCH"))
        val result = predictResponseMapper.map(mockResponseModel)
        expectedResult = getExpectedResult("SEARCH", 100F, true, 100F)
        result shouldContainAll expectedResult

        result.count() shouldBe 2
        result[0] shouldBe expectedResult[0]
        result[1] shouldBe expectedResult[1]
    }

    @Test
    fun testMap_shouldNotCrash_whenParsedValuesAreNull() {
        whenever(mockResponseModel.body).thenReturn(getBodyFor("SEARCH", "null", "null", "null"))
        val expectedResult = getExpectedResult("SEARCH", null, null, null)[0]

        val result = predictResponseMapper.map(mockResponseModel)[0]

        result shouldBe expectedResult
    }

    @Test
    fun testMap_withCart_shouldPreserveOrder() {
        whenever(mockResponseModel.body).thenReturn(getBodyFor("CART"))
        val result = predictResponseMapper.map(mockResponseModel)
        expectedResult = getExpectedResult("CART", 100F, true, 100F)

        result shouldContainAll expectedResult

        result.count() shouldBe 2
        result[0] shouldBe expectedResult[0]
        result[1] shouldBe expectedResult[1]
    }

    @Test
    fun testMap_withRelated_shouldPreserveOrder() {
        whenever(mockResponseModel.body).thenReturn(getBodyFor("RELATED"))
        val result = predictResponseMapper.map(mockResponseModel)
        expectedResult = getExpectedResult("RELATED", 100F, true, 100F)

        result shouldContainAll expectedResult

        result.count() shouldBe 2
        result[0] shouldBe expectedResult[0]
        result[1] shouldBe expectedResult[1]
    }

    @Test
    fun testMap_withResponseWithoutProducts() {
        whenever(mockResponseModel.body).thenReturn(getEmptyBodyFor("SEARCH"))
        val result = predictResponseMapper.map(mockResponseModel)

        expectedResult = emptyList()

        result shouldBe expectedResult
    }

    @Test
    fun testMap_shouldNotBreakWhenFloatPropertyIsNull() {
        whenever(mockResponseModel.body).thenReturn(getBodyFor(feature = "SEARCH", msrp = null))
        val result = predictResponseMapper.map(mockResponseModel)

        expectedResult = getExpectedResult("SEARCH", null, true, 100.0F)

        result shouldBe expectedResult
    }

    private fun getExpectedResult(feature: String, msrp: Float?, available: Boolean?, price: Float?): List<Product> {
        val product1 = Product(
                productId = "2119",
                title = "LSL Men Polo Shirt SE16",
                linkUrl = "http://lifestylelabels.com/lsl-men-polo-shirt-se16.html",
                feature = feature,
                cohort = "AAAA",
                categoryPath = "MEN>Shirts",
                available = available,
                msrp = msrp,
                price = price,
                imageUrlString = "http://lifestylelabels.com/pub/media/catalog/product/m/p/mp001.jpg",
                zoomImageUrlString = "http://lifestylelabels.com/pub/media/catalog/product/m/p/mp001.jpg",
                productDescription = "product Description",
                album = "album",
                actor = "actor",
                artist = "artist",
                author = "author",
                brand = "brand",
                year = 2000,
                customFields = mapOf(
                        "msrp_gpb" to "83.2",
                        "price_gpb" to "83.2",
                        "msrp_aed" to "100",
                        "price_aed" to "100",
                        "msrp_cad" to "100",
                        "price_cad" to "100",
                        "msrp_mxn" to "2057.44",
                        "price_mxn" to "2057.44",
                        "msrp_pln" to "100",
                        "price_pln" to null,
                        "msrp_rub" to "100",
                        "price_rub" to "100",
                        "msrp_sek" to "100",
                        "price_sek" to "100",
                        "msrp_try" to "339.95",
                        "price_try" to "339.95",
                        "msrp_usd" to "100",
                        "price_usd" to "100"
                ))
        return listOf(
                product1,
                Product(
                        "2120",
                        "LSL Men Polo Shirt SE16",
                        "http://lifestylelabels.com/lsl-men-polo-shirt-se16.html",
                        feature,
                        "AAAA")
        )
    }

    private fun getBodyFor(feature: String, msrp: String? = "100", available: String = "true", price: String = "100.0"): String {
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
                 "available":$available,
                 "msrp":$msrp,
                 "price":$price,
                 "msrp_gpb":"83.2",
                 "price_gpb":"83.2",
                 "msrp_aed":"100",
                 "price_aed":"100",
                 "msrp_cad":"100",
                 "price_cad":"100",
                 "msrp_mxn":"2057.44",
                 "price_mxn":"2057.44",
                 "msrp_pln":"100",
                 "price_pln":"null",
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

    private fun getEmptyBodyFor(feature: String): String {
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
                 "items":[]
              }
           }
        }"""
    }
}

