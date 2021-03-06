package com.emarsys.config.model

import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.util.log.LogLevel

data class RemoteConfig(val eventServiceUrl: String? = null,
                        val clientServiceUrl: String? = null,
                        val predictServiceUrl: String? = null,
                        val mobileEngageV2ServiceUrl: String? = null,
                        val deepLinkServiceUrl: String? = null,
                        val inboxServiceUrl: String? = null,
                        val messageInboxServiceUrl: String? = null,
                        val logLevel: LogLevel? = null,
                        val features: Map<InnerFeature, Boolean>? = null)