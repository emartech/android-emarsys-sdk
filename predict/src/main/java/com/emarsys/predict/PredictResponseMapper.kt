package com.emarsys.predict

import com.emarsys.core.Mapper
import com.emarsys.core.Mockable
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.util.JsonUtils.toFlatMapIncludingNulls
import com.emarsys.core.util.log.Logger.Companion.error
import com.emarsys.core.util.log.entry.CrashLog
import com.emarsys.predict.api.model.Product
import org.json.JSONException
import org.json.JSONObject
import java.util.*

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
@Mockable
class PredictResponseMapper : Mapper<ResponseModel, List<Product>> {
    override fun map(responseModel: ResponseModel): List<Product> {
        val result: MutableList<Product> = ArrayList()
        try {
            val jsonResponse = JSONObject(responseModel.body)
            if (jsonResponse.has("products")) {
                val products = jsonResponse.getJSONObject("products")
                val features = jsonResponse.getJSONObject("features")
                val cohort = jsonResponse.getString("cohort")

                features.keys().forEach { logicName ->
                    val feature = features.getJSONObject(logicName)
                    val productOrder = feature.getJSONArray("items")
                    for (i in 0 until productOrder.length()) {
                        val productJson = products.getJSONObject(productOrder.getJSONObject(i).getString("id"))
                        val productFields = toFlatMapIncludingNulls(productJson)
                        val product = createProductFromFields(logicName, cohort, productFields)
                        result.add(product)
                    }
                }
            }
        } catch (e: JSONException) {
            error(CrashLog(e))
        }
        return result
    }

    private fun createProductFromFields(feature: String, cohort: String, productFields: Map<String, String?>): Product {
        val mutableProductFields = productFields.toMutableMap()
        val msrp: String? = mutableProductFields.remove("msrp")
        val price: String? = mutableProductFields.remove("price")
        val available: String? = mutableProductFields.remove("available")
        return Product(
                productId = mutableProductFields.remove("item")!!,
                title = mutableProductFields.remove("title")!!,
                linkUrl = mutableProductFields.remove("link")!!,
                feature = feature,
                cohort = cohort,
                categoryPath = mutableProductFields.remove("category"),
                available = available?.toBoolean(),
                msrp = msrp?.toFloat(),
                price = price?.toFloat(),
                imageUrlString = mutableProductFields.remove("image"),
                zoomImageUrlString = mutableProductFields.remove("zoom_image"),
                productDescription = mutableProductFields.remove("description"),
                album = mutableProductFields.remove("album"),
                actor = mutableProductFields.remove("actor"),
                artist = mutableProductFields.remove("artist"),
                author = mutableProductFields.remove("author"),
                brand = mutableProductFields.remove("brand"),
                year = mutableProductFields.remove("year")?.toInt(),
                customFields = mutableProductFields)
    }
}