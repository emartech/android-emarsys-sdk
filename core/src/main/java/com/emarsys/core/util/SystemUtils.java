package com.emarsys.core.util;

public class SystemUtils {

    public static boolean isClassFound(String className) {
        boolean classExists = true;

        try {
            Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            classExists = false;
        }

        return classExists;
    }

    public static boolean isKotlinEnabled() {
        return isClassFound("kotlin.Pair");
    }
}
