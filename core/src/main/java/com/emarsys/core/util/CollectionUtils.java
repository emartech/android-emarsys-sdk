package com.emarsys.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public static <T> List<T> mergeLists(List<T>... lists) {
        Assert.notNull(lists, "Lists must not be null!");
        Assert.notEmpty(lists, "At least one argument must be provided!");
        Assert.elementsNotNull(lists, "Lists array cannot contain null elements!");

        List<T> merged = new ArrayList<>();

        for (List<T> list : lists) {
            merged.addAll(list);
        }

        return merged;
    }

}
