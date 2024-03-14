package com.emarsys.core.device

import com.emarsys.core.Mockable

@Mockable
data class HardwareIdentification(
    val hardwareId: String,
    val encryptedHardwareId: String? = null,
    val salt: String? = null,
    val iv: String? = null
)