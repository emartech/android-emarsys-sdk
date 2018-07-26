package com.emarsys.core.util;

import java.util.HashMap;
import java.util.Map;

public class CollectionUtils {

    public static <K, V> Map<K, V> mergeMaps(Map<K, V>... maps) {
        Assert.notNull(maps, "Maps must not be null!");
        Assert.notEmpty(maps, "At least one argument must be provided!");
        Assert.elementsNotNull(maps, "Maps array cannot contain null elements!");

        Map<K, V> merged = new HashMap<>();

        for (Map<K, V> map : maps) {
            merged.putAll(map);
        }

        return merged;
    }

}
