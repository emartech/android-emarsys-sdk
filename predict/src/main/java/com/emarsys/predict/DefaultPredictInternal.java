package com.emarsys.predict;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.api.ResponseErrorException;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.storage.KeyValueStore;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.JsonUtils;
import com.emarsys.predict.api.model.CartItem;
import com.emarsys.predict.api.model.Product;
import com.emarsys.predict.request.PredictRequestContext;
import com.emarsys.predict.request.PredictRequestModelFactory;
import com.emarsys.predict.util.CartItemUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultPredictInternal implements PredictInternal {


    public static final String VISITOR_ID_KEY = "predict_visitor_id";
    public static final String CONTACT_ID_KEY = "predict_contact_id";

    private static final String TYPE_CART = "predict_cart";
    private static final String TYPE_PURCHASE = "predict_purchase";
    private static final String TYPE_ITEM_VIEW = "predict_item_view";
    private static final String TYPE_CATEGORY_VIEW = "predict_category_view";
    private static final String TYPE_SEARCH_TERM = "predict_search_term";

    private final UUIDProvider uuidProvider;
    private final TimestampProvider timestampProvider;
    private final KeyValueStore keyValueStore;
    private final RequestManager requestManager;
    private final PredictRequestModelFactory requestModelFactory;

    public DefaultPredictInternal(PredictRequestContext requestContext, RequestManager requestManager, PredictRequestModelFactory requestModelFactory) {
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(requestModelFactory, "RequestModelFactory must not be null!");

        this.keyValueStore = requestContext.getKeyValueStore();
        this.requestManager = requestManager;
        this.uuidProvider = requestContext.getUuidProvider();
        this.timestampProvider = requestContext.getTimestampProvider();
        this.requestModelFactory = requestModelFactory;
    }

    @Override
    public void setContact(String contactId) {
        Assert.notNull(contactId, "ContactId must not be null!");
        keyValueStore.putString(CONTACT_ID_KEY, contactId);
    }

    @Override
    public void clearContact() {
        keyValueStore.remove(CONTACT_ID_KEY);
        keyValueStore.remove(VISITOR_ID_KEY);
    }

    @Override
    public String trackCart(List<CartItem> items) {
        Assert.notNull(items, "Items must not be null!");
        Assert.elementsNotNull(items, "Item elements must not be null!");

        ShardModel shard = new ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE_CART)
                .payloadEntry("cv", 1)
                .payloadEntry("ca", CartItemUtils.cartItemsToQueryParam(items))
                .build();

        requestManager.submit(shard);
        return shard.getId();
    }

    @Override
    public String trackPurchase(String orderId, List<CartItem> items) {
        Assert.notNull(orderId, "OrderId must not be null!");
        Assert.notNull(items, "Items must not be null!");
        Assert.elementsNotNull(items, "Item elements must not be null!");

        ShardModel shard = new ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE_PURCHASE)
                .payloadEntry("oi", orderId)
                .payloadEntry("co", CartItemUtils.cartItemsToQueryParam(items))
                .build();

        requestManager.submit(shard);
        return shard.getId();
    }

    @Override
    public String trackItemView(String itemId) {
        Assert.notNull(itemId, "ItemId must not be null!");

        ShardModel shard = new ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE_ITEM_VIEW)
                .payloadEntry("v", "i:" + itemId)
                .build();

        requestManager.submit(shard);
        return shard.getId();
    }

    @Override
    public String trackCategoryView(String categoryPath) {
        Assert.notNull(categoryPath, "CategoryPath must not be null!");

        ShardModel shard = new ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE_CATEGORY_VIEW)
                .payloadEntry("vc", categoryPath)
                .build();

        requestManager.submit(shard);
        return shard.getId();
    }

    @Override
    public String trackSearchTerm(String searchTerm) {
        Assert.notNull(searchTerm, "SearchTerm must not be null!");

        ShardModel shard = new ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE_SEARCH_TERM)
                .payloadEntry("q", searchTerm)
                .build();

        requestManager.submit(shard);
        return shard.getId();
    }

    @Override
    public void recommendProducts(final ResultListener<Try<List<Product>>> resultListener) {
        Assert.notNull(resultListener, "ResultListener must not be null!");

        RequestModel requestModel = requestModelFactory.createRecommendationRequest();

        requestManager.submitNow(requestModel, new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {

                List<Product> products = parseResponse(responseModel);

                resultListener.onResult(Try.success(products));
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                resultListener.onResult(Try.failure(new ResponseErrorException(
                        responseModel.getStatusCode(),
                        responseModel.getMessage(),
                        responseModel.getBody())));
            }

            @Override
            public void onError(String id, Exception cause) {
                resultListener.onResult(Try.failure(cause));
            }
        });
    }

    private List<Product> parseResponse(ResponseModel responseModel) {
        List<Product> result = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(responseModel.getBody());
            JSONObject products = jsonResponse.getJSONObject("products");
            for (int i = 0; i < products.names().length(); i++) {
                JSONObject product = products.getJSONObject(products.names().getString(i));

                Map<String, String> productFields = JsonUtils.toFlatMap(product);

                Product productBuilder = buildProductFromFields(productFields);
                result.add(productBuilder);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Product buildProductFromFields(Map<String, String> productFields) {
        String msrp = productFields.remove("msrp");
        String price = productFields.remove("price");
        String available = productFields.remove("available");

        Product.Builder productBuilder = new Product.Builder(
                productFields.remove("item"),
                productFields.remove("title"),
                productFields.remove("link")
        );
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
