package com.emarsys.core.feature;

import com.emarsys.core.api.experimental.FlipperFeature;

import java.util.HashSet;
import java.util.Set;

public class FeatureRegistry {

    static Set<String> enabledFeatures = new HashSet<>();

    public static boolean isFeatureEnabled(FlipperFeature feature) {
        return enabledFeatures.contains(feature.getName());
    }

    public static void enableFeature(FlipperFeature feature) {
        enabledFeatures.add(feature.getName());
    }

    static void reset() {
        enabledFeatures.clear();
    }

    public static void disableFeature(FlipperFeature feature) {
        enabledFeatures.remove(feature.getName());
    }
}
