package com.emarsys.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.emarsys.Emarsys;
import com.emarsys.predict.api.model.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PredictFragment extends BaseFragment {

    private EditText itemView;
    private EditText categoryView;
    private EditText searchTerm;
    private EditText orderId;
    private EditText cartItemsContainer;
    private List<CartItem> cartItems = new ArrayList<>();
    private Random random = new Random();

    @Override
    public String getName() {
        return "Predict";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_predict, container, false);

        itemView = root.findViewById(R.id.itemView);
        categoryView = root.findViewById(R.id.categoryView);
        searchTerm = root.findViewById(R.id.searchTerm);
        orderId = root.findViewById(R.id.orderId);
        cartItemsContainer = root.findViewById(R.id.cartItems);

        root.findViewById(R.id.trackItemView).setOnClickListener(view -> {
            String itemId = itemView.getText().toString();
            if (!itemId.isEmpty()) {
                Emarsys.Predict.trackItemView(itemId);
            }
        });

        root.findViewById(R.id.trackCategoryView).setOnClickListener(view -> {
            String categoryText = categoryView.getText().toString();
            if (!categoryText.isEmpty()) {
                Emarsys.Predict.trackCategoryView(categoryView.getText().toString());
            }
        });

        root.findViewById(R.id.trackSearchTerm).setOnClickListener(view -> {
            String searchTermText = searchTerm.getText().toString();
            if (!searchTermText.isEmpty()) {
                Emarsys.Predict.trackSearchTerm(searchTerm.getText().toString());
            }
        });

        root.findViewById(R.id.addCartItem).setOnClickListener(view -> {
            CartItem cartItem = generateCartItem();
            cartItems.add(cartItem);
            cartItemsContainer.getText().append(cartItem.toString());

        });

        root.findViewById(R.id.trackCartItems).setOnClickListener(view -> {
            if (!cartItems.isEmpty()) {
                Emarsys.Predict.trackCart(cartItems);
            }
        });

        root.findViewById(R.id.trackPurchase).setOnClickListener(view -> {
            String orderIdText = orderId.getText().toString();
            if (!cartItems.isEmpty()) {
                Emarsys.Predict.trackPurchase(orderIdText, cartItems);
            }
        });
        return root;
    }

    private CartItem generateCartItem() {
        return new SampleCartItem(getRandomItemId(), 1 + random.nextInt(99), 1 + random.nextInt(5));
    }

    private String getRandomItemId() {
        String identifiers[] = new String[]{"2156",
                "2157",
                "2158",
                "2159",
                "2160",
                "2161",
                "2163",
                "2164",
                "2165",
                "2166",
                "2167",
                "2182",
                "2169",
                "2170",
                "2171",
                "2172",
                "2173",
                "2174",
                "2175",
                "2176",
                "2177",
                "2178",
                "2179",
                "2180",
                "2181",
                "2168",
                "2183",
                "2162",
                "2184"};
        return identifiers[random.nextInt(identifiers.length)];
    }
}