package com.emarsys.mobileengage.testUtil;

import java.util.List;
import java.util.NoSuchElementException;

public class CollectionTestUtils {
    public static int numberOfElementsIn(List list, Class type) {
        int count = 0;

        for (Object object : list) {
            if (object.getClass().equals(type)) {
                count++;
            }
        }

        return count;
    }

    public static int numberOfElementsIn(Object[] array, Class type) {
        int count = 0;

        for (Object object : array) {
            if (object.getClass().equals(type)) {
                count++;
            }
        }

        return count;
    }

    public static <T> T getElementByType(List<?> list, Class<T> type) {
        for (Object o : list) {
            if (type.isInstance(o)) {
                return type.cast(o);
            }
        }
        throw new NoSuchElementException("Cannot find element of class " + type + " in " + list);
    }
}
