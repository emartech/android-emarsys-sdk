package com.emarsys.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.emarsys.Emarsys;

public class PredictFragment extends BaseFragment {

    private EditText itemView;
    private EditText categoryView;
    private EditText searchTerm;

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

        root.findViewById(R.id.trackItemView).setOnClickListener(view -> {
            String itemId = itemView.getText().toString();
            if (!itemId.isEmpty()) {
                Emarsys.Predict.trackItemView(itemId);
            }
        });
        root.findViewById(R.id.trackCategoryView).setOnClickListener(view -> {
            String categoryText = categoryView.getText().toString();
            if (!categoryText.isEmpty()) {
                Emarsys.Predict.trackItemView(categoryView.getText().toString());
            }
        });
        root.findViewById(R.id.trackSearchTerm).setOnClickListener(view -> {
            String searchTermText = searchTerm.getText().toString();
            if (!searchTermText.isEmpty()) {
                Emarsys.Predict.trackItemView(searchTerm.getText().toString());
            }
        });
        return root;
    }
}