package com.emarsys.core.provider.uuid

import com.emarsys.core.Mockable
import java.util.UUID

@Mockable
class UUIDProvider {
    fun provideId(): String = UUID.randomUUID().toString()
}
