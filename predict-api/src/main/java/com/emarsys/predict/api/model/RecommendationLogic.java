package com.emarsys.predict.api.model;

import androidx.annotation.NonNull;

import com.emarsys.core.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RecommendationLogic implements Logic {

    private String logicName;
    private Map<String, String> data;

    private RecommendationLogic(String logicName, Map<String, String> data) {
        this.logicName = logicName;
        this.data = data;
    }

    public static Logic search() {
        Map<String, String> data = new HashMap<>();
        data.put("q", "");
        return new RecommendationLogic("SEARCH", data);
    }

    public static Logic search(@NonNull String searchTerm) {
        Assert.notNull(searchTerm, "SearchTerm must not be null!");

        Map<String, String> data = new HashMap<>();
        data.put("q", searchTerm);
        return new RecommendationLogic("SEARCH", data);
    }

    @Override
    public String getLogicName() {
        return logicName;
    }

    @Override
    public Map<String, String> getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecommendationLogic that = (RecommendationLogic) o;
        return Objects.equals(logicName, that.logicName) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logicName, data);
    }

    @Override
    public String toString() {
        return "RecommendationLogic{" +
                "logicName='" + logicName + '\'' +
                ", data=" + data +
                '}';
    }
}