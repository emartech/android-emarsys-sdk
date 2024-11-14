package com.emarsys.core.device

import com.emarsys.core.Mockable

@Mockable
data class ClientIdentification(
    val clientId: String,
    val encryptedClientId: String? = null,
    val salt: String? = null,
    val iv: String? = null
)