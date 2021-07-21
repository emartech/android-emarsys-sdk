package com.emarsys.core.endpoint

import com.emarsys.core.Mockable
import com.emarsys.core.storage.Storage

@Mockable
class ServiceEndpointProvider(private val serviceUrlStorage: Storage<String?>, private val defaultEndpoint: String) {

    fun provideEndpointHost(): String {
        return serviceUrlStorage.get() ?: defaultEndpoint
    }
}