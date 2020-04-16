package com.emarsys

import com.emarsys.core.Mockable
import com.emarsys.core.endpoint.Endpoint
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestHeaderUtils

@Mockable
class EmarsysRequestModelFactory(private val mobileEngageRequestContext: MobileEngageRequestContext) {

    fun createRemoteConfigRequest(): RequestModel {
        return RequestModel.Builder(mobileEngageRequestContext.timestampProvider, mobileEngageRequestContext.uuidProvider)
                .method(RequestMethod.GET)
                .url(Endpoint.remoteConfigUrl(mobileEngageRequestContext.applicationCode))
                .headers(RequestHeaderUtils.createBaseHeaders_V3(mobileEngageRequestContext))
                .build()
    }

    fun createRemoteConfigSignatureRequest(): RequestModel {
        return RequestModel.Builder(mobileEngageRequestContext.timestampProvider, mobileEngageRequestContext.uuidProvider)
                .method(RequestMethod.GET)
                .url(Endpoint.remoteConfigSignatureUrl(mobileEngageRequestContext.applicationCode))
                .headers(RequestHeaderUtils.createBaseHeaders_V3(mobileEngageRequestContext))
                .build()    }
}