package com.emarsys.config.model

data class RemoteConfig(val eventServiceUrl: String? = null,
                        val clientServiceUrl: String? = null,
                        val predictServiceUrl: String? = null,
                        val mobileEngageV2ServiceUrl: String? = null,
                        val deepLinkServiceUrl: String? = null,
                        val inboxServiceUrl: String? = null)