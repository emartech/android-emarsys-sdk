package com.emarsys.mobileengage.util;

import android.os.Build;

public class AndroidVersionUtils {

    public static boolean isKitKatOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

}
