package com.emarsys.predict

import com.emarsys.core.response.ResponseModel
import com.emarsys.predict.api.model.Product
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class PredictResponseMapperTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test
    fun testMap() {
        val expectedResult = listOf(Product.Builder(
                "2119",
                "LSL Men Polo Shirt SE16",
                "http://lifestylelabels.com/lsl-men-polo-shirt-se16.html")
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
                        "http://lifestylelabels.com/lsl-men-polo-shirt-se16.html")
                        .build())
        val mockResponseModel = Mockito.mock(ResponseModel::class.java)
        whenever(mockResponseModel.body).thenReturn("""{
           "cohort":"AAAA",
           "visitor":"16BCC0D2745E6B36",
           "session":"24E844D1E58C1C2",
           "features":{
              "SEARCH":{
                 "hasMore":true,
                 "merchants":[
                    "1428C8EE286EC34B"
                 ],
                 "items":[
                    {
                       "id":"2119",
                       "id":"2120"
                    }
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
        )
        val predictResponseMapper = PredictResponseMapper()
        val result = predictResponseMapper.map(mockResponseModel)

        result shouldBe expectedResult
    }

}

