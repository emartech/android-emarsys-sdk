package com.emarsys.core.util

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

object AndroidVersionUtils {
    val isOreoOrAbove: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    val isBelowOreo: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.O
    val isBelowQ: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    val isBelow30: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.R
    val isBelowS: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    val isBelowTiramisu: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU

    val isTiramisuOrAbove: Boolean
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    val isUOrAbove: Boolean
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    val isBelowUpsideDownCake: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE
}