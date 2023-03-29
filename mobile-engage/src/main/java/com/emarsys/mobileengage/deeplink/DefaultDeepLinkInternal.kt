package com.emarsys.mobileengage.deeplink;

import static com.emarsys.mobileengage.endpoint.Endpoint.deepLinkBase;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.endpoint.ServiceEndpointProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageRequestContext;

import java.util.HashMap;
import java.util.Map;

public class DefaultDeepLinkInternal implements DeepLinkInternal {
    private static final String TAG = "Emarsys SDK - DeepLink";

    private static final String EMS_DEEP_LINK_TRACKED_KEY = "ems_deep_link_tracked";
    private final MobileEngageRequestContext requestContext;
    private final ServiceEndpointProvider deepLinkServiceProvider;
    private final RequestManager manager;

    public DefaultDeepLinkInternal(RequestManager manager, MobileEngageRequestContext requestContext, ServiceEndpointProvider deepLinkServiceProvider) {
        Assert.notNull(manager, "RequestManager must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(deepLinkServiceProvider, "DeepLinkServiceProvider must not be null!");

        this.manager = manager;
        this.requestContext = requestContext;
        this.deepLinkServiceProvider = deepLinkServiceProvider;
    }

    @Override
    public void trackDeepLinkOpen(Activity activity, Intent intent, CompletionListener completionListener) {
        Uri uri = intent.getData();
        Intent intentFromActivity = activity.getIntent();
        boolean isLinkTracked = intentFromActivity.getBooleanExtra(EMS_DEEP_LINK_TRACKED_KEY, false);

        if (!isLinkTracked && uri != null) {
            String ems_dl = "ems_dl";
            String deepLinkQueryParam = null;

            try {
                deepLinkQueryParam = uri.getQueryParameter(ems_dl);
            } catch (UnsupportedOperationException ignored) {
                Log.e(TAG, String.format("Deep-link URI %1$s is not hierarchical", uri));
            }

            if (deepLinkQueryParam != null) {
                HashMap<String, Object> payload = new HashMap<>();
                payload.put(ems_dl, deepLinkQueryParam);

                RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUuidProvider())
                        .url(deepLinkServiceProvider.provideEndpointHost() + deepLinkBase())
                        .headers(createHeaders())
                        .payload(payload)
                        .build();

                intentFromActivity.putExtra(EMS_DEEP_LINK_TRACKED_KEY, true);
                manager.submit(model, completionListener);
            }
        }
    }

    private Map<String, String> createHeaders() {
        Map<String, String> headers = new HashMap<>();

        String userAgentValue = String.format(
                "Emarsys SDK %s Android %s",
                requestContext.getDeviceInfo().getSdkVersion(),
                Build.VERSION.SDK_INT);
        headers.put("User-Agent", userAgentValue);

        return headers;
    }

}
