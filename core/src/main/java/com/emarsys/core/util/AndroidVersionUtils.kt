package com.emarsys.core.util

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

object AndroidVersionUtils {
    val isOreoOrAbove: Boolean
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    val isBelowOreo: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.O

    val isBelowQ: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    val isBelowTiramisu: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU

    val isSOrAbove: Boolean
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val isBelowUpsideDownCake: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    val isUpsideDownCakeOrHigher: Boolean
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    val isUpsideDownCake: Boolean
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        get() = Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    val isVanillaIceCreamOrHigher: Boolean
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.VANILLA_ICE_CREAM)
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
}