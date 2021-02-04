package com.emarsys.core.util;

import android.os.Build;

public class AndroidVersionUtils {

    public static boolean isOreoOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }
    public static boolean isNougatOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isBelowMarshmallow() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    }

    public static boolean isBelowOreo() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O;
    }

    public static boolean isBelowQ() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q;
    }
}
