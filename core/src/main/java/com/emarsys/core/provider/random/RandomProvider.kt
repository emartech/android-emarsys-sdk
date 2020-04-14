package com.emarsys.core.provider.random

import com.emarsys.core.Mockable
import kotlin.random.Random

@Mockable
class RandomProvider {

    fun provideDouble(until: Double): Double {
        return Random.nextDouble(until)
    }
}