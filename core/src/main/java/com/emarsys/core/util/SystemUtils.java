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

    public static String getCallerMethodName() {
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }
}
