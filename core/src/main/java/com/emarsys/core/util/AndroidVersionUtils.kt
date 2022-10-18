package com.emarsys.core.util;

import android.os.Build;

public class AndroidVersionUtils {

    public static boolean isOreoOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean isBelowOreo() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O;
    }

    public static boolean isBelowQ() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q;
    }

    public static boolean isBelowS() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S;
    }
}
