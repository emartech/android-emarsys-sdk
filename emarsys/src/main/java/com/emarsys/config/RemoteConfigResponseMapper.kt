package com.emarsys.config

import com.emarsys.config.model.RemoteConfig
import com.emarsys.core.Mapper
import com.emarsys.core.Mockable
import com.emarsys.core.response.ResponseModel
import org.json.JSONObject

@Mockable
class RemoteConfigResponseMapper : Mapper<ResponseModel, RemoteConfig> {
    override fun map(responseModel: ResponseModel?): RemoteConfig {
        var remoteConfig = RemoteConfig()
        if (responseModel != null) {
            val jsonResponse = JSONObject(responseModel.body)
            if (jsonResponse.has("serviceUrls")) {
                val serviceUrls = jsonResponse.getJSONObject("serviceUrls")
                remoteConfig = remoteConfig.copy(
                        eventServiceUrl = serviceUrls.optString("eventService", null),
                        clientServiceUrl = serviceUrls.optString("clientService", null),
                        deepLinkServiceUrl = serviceUrls.optString("deepLinkService", null),
                        inboxServiceUrl = serviceUrls.optString("inboxService", null),
                        messageInboxServiceUrl = serviceUrls.optString("messageInboxService", null),
                        mobileEngageV2ServiceUrl = serviceUrls.optString("mobileEngageV2Service", null),
                        predictServiceUrl = serviceUrls.optString("predictService", null)
                )
            }
        }
        return remoteConfig
    }
}