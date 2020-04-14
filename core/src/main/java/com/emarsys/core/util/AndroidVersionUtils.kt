package com.emarsys.core.util

import android.os.Build

object AndroidVersionUtils {
    val isKitKatOrAbove: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

    val isOreoOrAbove: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    val isLollipopOrAbove: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    val isBelowMarshmallow: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M

    val isBelowQ: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
}