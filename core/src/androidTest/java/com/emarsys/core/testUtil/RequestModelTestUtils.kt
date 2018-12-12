package com.emarsys.core.testUtil

import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.request.model.RequestMethod.*
import com.emarsys.testUtil.TestUrls

object RequestModelTestUtils {

    @JvmStatic
    @JvmOverloads
    fun createRequestModel(
            method: RequestMethod = GET,
            url: String = TestUrls.customResponse(200)): RequestModel =
            RequestModel.Builder(TimestampProvider(), UUIDProvider())
                    .url(url)
                    .method(method)
                    .headers(mapOf(
                            "accept" to "application/json",
                            "content" to "application/x-www-form-urlencoded"
                    ))
                    .build()

}
