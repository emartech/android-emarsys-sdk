package com.emarsys.core.provider.timestamp

import com.emarsys.core.Mockable

@Mockable
class TimestampProvider {
    fun provideTimestamp(): Long = System.currentTimeMillis()
}
