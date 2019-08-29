package com.emarsys.predict;

import com.emarsys.core.Mapper;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.JsonUtils;
import com.emarsys.predict.api.model.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PredictResponseMapper implements Mapper<ResponseModel, List<Product>> {

    @Override
    public List<Product> map(ResponseModel responseModel) {
        List<Product> result = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(responseModel.getBody());
            JSONObject products = jsonResponse.getJSONObject("products");
            JSONObject features = jsonResponse.getJSONObject("features");
            String cohort = jsonResponse.getString("cohort");
            Iterator<String> keys = features.keys();
            while (keys.hasNext()) {
                String logicName = keys.next();
                JSONObject feature = features.getJSONObject(logicName);
                JSONArray productOrder = feature.getJSONArray("items");

                for (int i = 0; i < productOrder.length(); i++) {
                    JSONObject product = products.getJSONObject(productOrder.getJSONObject(i).getString("id"));

                    Map<String, String> productFields = JsonUtils.toFlatMap(product);

                    Product productBuilder = buildProductFromFields(logicName, cohort, productFields);
                    result.add(productBuilder);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Product buildProductFromFields(String feature, String cohort, Map<String, String> productFields) {
        String msrp = productFields.remove("msrp");
        String price = productFields.remove("price");
        String available = productFields.remove("available");

        Product.Builder productBuilder = new Product.Builder(
                productFields.remove("item"),
                productFields.remove("title"),
                productFields.remove("link"),
                feature,
                cohort);
        productBuilder.categoryPath(productFields.remove("category"));
        if (available != null) {
            productBuilder.available(Boolean.valueOf(available));
        }
        if (msrp != null) {
            productBuilder.msrp(Float.parseFloat(msrp));
        }
        if (price != null) {
            productBuilder.price(Float.parseFloat(price));
        }
        productBuilder.imageUrl(productFields.remove("image"));
        productBuilder.zoomImageUrl(productFields.remove("zoom_image"));
        productBuilder.productDescription(productFields.remove("description"));
        productBuilder.album(productFields.remove("album"));
        productBuilder.actor(productFields.remove("actor"));
        productBuilder.artist(productFields.remove("artist"));
        productBuilder.author(productFields.remove("author"));
        productBuilder.brand(productFields.remove("brand"));
        String year = productFields.remove("year");
        if (year != null) {
            productBuilder.year(Integer.parseInt(year));
        }
        productBuilder.customFields(productFields);
        return productBuilder.build();
    }
}