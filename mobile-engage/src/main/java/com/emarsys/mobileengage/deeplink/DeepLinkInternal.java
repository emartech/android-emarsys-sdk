package com.emarsys.mobileengage.deeplink;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.MobileEngageUtils;
import com.emarsys.mobileengage.RequestContext;

import java.util.HashMap;
import java.util.Map;

import static com.emarsys.mobileengage.endpoint.Endpoint.DEEP_LINK_CLICK;

public class DeepLinkInternal {

    private static final String EMS_DEEP_LINK_TRACKED_KEY = "ems_deep_link_tracked";
    private final RequestContext requestContext;

    private final RequestManager manager;

    public DeepLinkInternal(RequestManager manager, RequestContext requestContext) {
        Assert.notNull(manager, "RequestManager must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");
        this.manager = manager;
        this.requestContext = requestContext;
    }

    public void trackDeepLinkOpen(Activity activity, Intent intent) {
        Uri uri = intent.getData();
        Intent intentFromActivity = activity.getIntent();
        boolean isLinkTracked = intentFromActivity.getBooleanExtra(EMS_DEEP_LINK_TRACKED_KEY, false);

        if (!isLinkTracked && uri != null) {
            String ems_dl = "ems_dl";
            String deepLinkQueryParam = uri.getQueryParameter(ems_dl);

            if (deepLinkQueryParam != null) {
                HashMap<String, Object> payload = new HashMap<>();
                payload.put(ems_dl, deepLinkQueryParam);

                RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getRequestIdProvider())
                        .url(DEEP_LINK_CLICK)
                        .headers(createHeaders())
                        .payload(payload)
                        .build();

                MobileEngageUtils.incrementIdlingResource();
                intentFromActivity.putExtra(EMS_DEEP_LINK_TRACKED_KEY, true);
                manager.submit(model);
            }
        }
    }

    private Map<String, String> createHeaders() {
        Map<String, String> headers = new HashMap<>();

        String userAgentValue = String.format(
                "Mobile Engage SDK %s Android %s",
                MobileEngageInternal.MOBILEENGAGE_SDK_VERSION,
                Build.VERSION.SDK_INT);
        headers.put("User-Agent", userAgentValue);

        return headers;
    }

}
