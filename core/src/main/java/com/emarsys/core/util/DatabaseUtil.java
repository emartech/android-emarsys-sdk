package com.emarsys.core.util;

public class DatabaseUtil {

    public static String generateInStatement(String columnName, String[] args) {
        StringBuilder sb = new StringBuilder(columnName + " IN (?");
        for (int i = 1; i < args.length; i++) {
            sb.append(", ?");
        }
        sb.append(")");
        return sb.toString();
    }
}
