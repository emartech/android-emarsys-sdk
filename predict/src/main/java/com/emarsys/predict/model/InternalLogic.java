package com.emarsys.predict.model;

import com.emarsys.core.util.Assert;
import com.emarsys.predict.api.model.Logic;
import com.emarsys.predict.api.model.RecommendationLogic;

import java.util.Map;

public class InternalLogic implements Logic {

    private final Logic logic;
    private final LastTrackedItemContainer lastTrackedItemContainer;

    public InternalLogic(Logic logic, LastTrackedItemContainer lastTrackedItemContainer) {
        Assert.notNull(logic, "Logic must not be null!");
        Assert.notNull(lastTrackedItemContainer, "LastTrackedContainer must not be null!");

        this.logic = logic;
        this.lastTrackedItemContainer = lastTrackedItemContainer;
    }

    @Override
    public String getLogicName() {
        return logic.getLogicName();
    }

    @Override
    public Map<String, String> getData() {
        Map<String, String> result = logic.getData();
        if (result.isEmpty()) {
            switch (logic.getLogicName()) {
                case RecommendationLogic.RELATED:
                    result = lastTrackedItemContainer.getLastItemView() == null ?
                            RecommendationLogic.related().getData() :
                            RecommendationLogic.related(lastTrackedItemContainer.getLastItemView()).getData();
                    break;
                case RecommendationLogic.CART:
                    result = lastTrackedItemContainer.getLastCartItems() == null ?
                            RecommendationLogic.cart().getData() :
                            RecommendationLogic.cart(lastTrackedItemContainer.getLastCartItems()).getData();
                    break;
                case RecommendationLogic.SEARCH:
                    result = lastTrackedItemContainer.getLastSearchTerm() == null ?
                            RecommendationLogic.search().getData() :
                            RecommendationLogic.search(lastTrackedItemContainer.getLastSearchTerm()).getData();
                    break;
                case RecommendationLogic.CATEGORY:
                    result = lastTrackedItemContainer.getLastCategoryPath() == null ?
                            RecommendationLogic.category().getData() :
                            RecommendationLogic.category(lastTrackedItemContainer.getLastCategoryPath()).getData();
                    break;
                case RecommendationLogic.ALSO_BOUGHT:
                    result = lastTrackedItemContainer.getLastItemView() == null ?
                            RecommendationLogic.alsoBought().getData() :
                            RecommendationLogic.alsoBought(lastTrackedItemContainer.getLastItemView()).getData();
                    break;
                case RecommendationLogic.POPULAR:
                    result = lastTrackedItemContainer.getLastCategoryPath() == null ?
                            RecommendationLogic.popular().getData() :
                            RecommendationLogic.popular(lastTrackedItemContainer.getLastCategoryPath()).getData();
                    break;
            }
        }
        return result;
    }

}
