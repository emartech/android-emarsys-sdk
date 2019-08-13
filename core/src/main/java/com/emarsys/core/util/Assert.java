package com.emarsys.core.util;

import java.util.List;

public final class Assert {

    private Assert() {
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            if (message == null) {
                message = "Argument must not be null!";
            }
            throw new IllegalArgumentException(message);
        }
    }

    public static void positiveInt(Integer integer, String message) {
        notNull(integer, null);
        if (integer < 1) {
            if (message == null) {
                message = "Parameter value must be positive!";
            }
            throw new IllegalArgumentException(message);
        }
    }

    public static void elementsNotNull(Object[] array, String message) {
        notNull(array, null);
        for (Object object : array) {
            notNull(object, message);
        }
    }

    public static void elementsNotNull(List<?> list, String message) {
        notNull(list, null);
        for (Object object : list) {
            notNull(object, message);
        }
    }

    public static void notEmpty(Object[] array, String message) {
        notNull(array, null);
        if (array.length == 0) {
            if (message == null) {
                message = "Argument must not be empty!";
            }
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(List<?> list, String message) {
        notNull(list, null);
        if (list.size() == 0) {
            if (message == null) {
                message = "Argument must not be empty!";
            }
            throw new IllegalArgumentException(message);
        }
    }
}
