package com.emarsys.mobileengage.inbox;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.api.ResponseErrorException;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.util.RequestHeaderUtils_Old;
import com.emarsys.mobileengage.util.RequestPayloadUtils;
import com.emarsys.mobileengage.util.RequestUrlUtils_Old;

import java.util.HashMap;
import java.util.Map;

import static com.emarsys.mobileengage.endpoint.Endpoint.INBOX_FETCH_V1;
import static com.emarsys.mobileengage.endpoint.Endpoint.INBOX_RESET_BADGE_COUNT_V1;

public class InboxInternal_V1 implements InboxInternal {

    private Handler handler;
    private NotificationCache cache;
    private RequestManager manager;
    private RequestContext requestContext;

    public InboxInternal_V1(
            RequestManager requestManager,
            RequestContext requestContext) {
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");

        this.handler = new Handler(Looper.getMainLooper());
        this.cache = new NotificationCache();
        this.manager = requestManager;
        this.requestContext = requestContext;
    }

    @Override
    public void fetchNotifications(final ResultListener<Try<NotificationInboxStatus>> resultListener) {
        Assert.notNull(resultListener, "ResultListener should not be null!");

        if (requestContext.getContactFieldValueStorage().get() != null) {
            handleFetchRequest(resultListener);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    resultListener.onResult(Try.failure(new NotificationInboxException("AppLogin must be called before calling fetchNotifications!")));
                }
            });
        }
    }

    private void handleFetchRequest(final ResultListener<Try<NotificationInboxStatus>> resultListener) {
        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(INBOX_FETCH_V1)
                .headers(createBaseHeaders(requestContext))
                .method(RequestMethod.GET)
                .build();

        manager.submitNow(model, new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {
                NotificationInboxStatus status = InboxParseUtils.parseNotificationInboxStatus(responseModel.getBody());
                NotificationInboxStatus resultStatus = new NotificationInboxStatus(cache.merge(status.getNotifications()), status.getBadgeCount());
                resultListener.onResult(Try.success(resultStatus));
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                resultListener.onResult(Try.failure(new ResponseErrorException(
                        responseModel.getStatusCode(),
                        responseModel.getMessage(),
                        responseModel.getBody())));
            }

            @Override
            public void onError(String id, Exception cause) {
                resultListener.onResult(Try.failure(cause));
            }
        });
    }

    @Override
    public void resetBadgeCount(final CompletionListener completionListener) {
        if (requestContext.getContactFieldValueStorage().get() != null) {
            handleResetRequest(completionListener);
        } else {
            if (completionListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        completionListener.onCompleted(new NotificationInboxException("AppLogin must be called before calling fetchNotifications!"));
                    }
                });
            }
        }
    }

    @Override
    public void trackNotificationOpen(Notification notification, CompletionListener completionListener) {
        Assert.notNull(notification, "Notification must not be null!");

        Map<String, Object> payload = RequestPayloadUtils.createBasePayload(requestContext);
        payload.put("source", "inbox");
        payload.put("sid", notification.getSid());
        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils_Old.createEventUrl_V2("message_open"))
                .payload(payload)
                .headers(RequestHeaderUtils_Old.createBaseHeaders_V2(requestContext))
                .build();

        manager.submit(model, completionListener);
    }

    @Override
    public void purgeNotificationCache() {
    }

    private void handleResetRequest(final CompletionListener resultListener) {
        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(INBOX_RESET_BADGE_COUNT_V1)
                .headers(createBaseHeaders(requestContext))
                .method(RequestMethod.POST)
                .build();

        manager.submitNow(model, new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {
                if (resultListener != null) {
                    resultListener.onCompleted(null);
                }
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                if (resultListener != null) {
                    resultListener.onCompleted(new ResponseErrorException(
                            responseModel.getStatusCode(),
                            responseModel.getMessage(),
                            responseModel.getBody()));
                }
            }

            @Override
            public void onError(String id, Exception cause) {
                if (resultListener != null) {
                    resultListener.onCompleted(cause);
                }
            }
        });
    }

    private Map<String, String> createBaseHeaders(RequestContext requestContext) {
        Map<String, String> result = new HashMap<>();

        result.put("x-ems-me-hardware-id", requestContext.getDeviceInfo().getHwid());
        result.put("x-ems-me-application-code", requestContext.getApplicationCode());
        result.put("x-ems-me-contact-field-id", String.valueOf(requestContext.getContactFieldId()));
        result.put("x-ems-me-contact-field-value", requestContext.getContactFieldValueStorage().get());

        result.putAll(RequestHeaderUtils_Old.createDefaultHeaders(requestContext));
        result.putAll(RequestHeaderUtils_Old.createBaseHeaders_V2(requestContext));

        return result;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

}
