package com.emarsys.config.model

data class RemoteConfig(private val eventServiceUrl: String? = null,
                        private val clientServiceUrl: String? = null,
                        private val predictServiceUrl: String? = null,
                        private val mobileEngageV2ServiceUrl: String? = null,
                        private val deepLinkServiceUrl: String? = null,
                        private val inboxServiceUrl: String? = null)