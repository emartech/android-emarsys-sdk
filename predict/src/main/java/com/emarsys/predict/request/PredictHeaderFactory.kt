package com.emarsys.predict.request

import com.emarsys.core.Mockable
import com.emarsys.predict.DefaultPredictInternal

@Mockable
class PredictHeaderFactory(private val requestContext: PredictRequestContext) {

    fun createBaseHeader(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        result["User-Agent"] = "EmarsysSDK|osversion:${requestContext.deviceInfo.osVersion}|platform:${requestContext.deviceInfo.platform}"

        val xp = requestContext.keyValueStore.getString(DefaultPredictInternal.XP_KEY)
        val visitorId = requestContext.keyValueStore.getString(DefaultPredictInternal.VISITOR_ID_KEY)

        if (xp != null || visitorId != null) {
            val cookies = buildString {
                if (xp != null) append("xp=$xp;")
                if (visitorId != null) append("cdv=$visitorId")
            }
            result["Cookie"] = cookies
        }

        return result
    }
}
