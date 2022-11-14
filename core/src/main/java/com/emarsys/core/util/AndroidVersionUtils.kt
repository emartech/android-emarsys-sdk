package com.emarsys.core.util

import android.os.Build

object AndroidVersionUtils {
    val isOreoOrAbove: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    val isBelowTiramisu: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    val isBelowOreo: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.O
    val isBelowQ: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    val isBelowS: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
}