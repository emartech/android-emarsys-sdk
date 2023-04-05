package com.emarsys.mobileengage.deeplink

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.endpoint.Endpoint

@Mockable
class DefaultDeepLinkInternal(
    private val requestContext: MobileEngageRequestContext,
    private val deepLinkServiceProvider: ServiceEndpointProvider,
    private val manager: RequestManager
): DeepLinkInternal {

    companion object {
        private const val TAG = "Emarsys SDK - DeepLink"
        private const val EMS_DEEP_LINK_TRACKED_KEY = "ems_deep_link_tracked"
    }

    override fun trackDeepLinkOpen(
        activity: Activity,
        intent: Intent,
        completionListener: CompletionListener?
    ) {
        val uri = intent.data
        val intentFromActivity: Intent? = activity.intent
        val isLinkTracked = intentFromActivity?.getBooleanExtra(EMS_DEEP_LINK_TRACKED_KEY, false) ?: false
        if (!isLinkTracked && uri != null) {
            val ems_dl = "ems_dl"
            var deepLinkQueryParam: String? = null
            try {
                deepLinkQueryParam = uri.getQueryParameter(ems_dl)
            } catch (ignored: UnsupportedOperationException) {
                Log.e(TAG, String.format("Deep-link URI %1\$s is not hierarchical", uri))
            }
            if (deepLinkQueryParam != null) {
                val payload = HashMap<String, Any?>()
                payload[ems_dl] = deepLinkQueryParam
                val model = RequestModel.Builder(
                    requestContext.timestampProvider,
                    requestContext.uuidProvider
                )
                    .url(deepLinkServiceProvider.provideEndpointHost() + Endpoint.deepLinkBase())
                    .headers(createHeaders())
                    .payload(payload)
                    .build()
                intentFromActivity?.putExtra(EMS_DEEP_LINK_TRACKED_KEY, true)
                manager.submit(model, completionListener)
            }
        }
    }

    private fun createHeaders(): Map<String, String> {
        val headers: MutableMap<String, String> = HashMap()
        val userAgentValue = String.format(
            "Emarsys SDK %s Android %s",
            requestContext.deviceInfo.sdkVersion,
            Build.VERSION.SDK_INT
        )
        headers["User-Agent"] = userAgentValue
        return headers
    }
}