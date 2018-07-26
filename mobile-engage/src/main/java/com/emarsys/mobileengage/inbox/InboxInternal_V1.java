package com.emarsys.mobileengage.inbox;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.MobileEngageException;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;
import com.emarsys.mobileengage.util.RequestHeaderUtils;
import com.emarsys.mobileengage.util.RequestPayloadUtils;
import com.emarsys.mobileengage.util.RequestUrlUtils;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import java.util.HashMap;
import java.util.Map;

import static com.emarsys.mobileengage.endpoint.Endpoint.INBOX_FETCH_V1;
import static com.emarsys.mobileengage.endpoint.Endpoint.INBOX_RESET_BADGE_COUNT_V1;

public class InboxInternal_V1 implements InboxInternal {

    Handler handler;
    RestClient client;
    NotificationCache cache;
    RequestManager manager;
    RequestContext requestContext;

    public InboxInternal_V1(
            RequestManager requestManager,
            RestClient restClient,
            RequestContext requestContext) {
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(restClient, "RestClient must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");
        EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: config %s, requestManager %s", requestContext.getConfig(), requestManager);

        this.client = restClient;
        this.handler = new Handler(Looper.getMainLooper());
        this.cache = new NotificationCache();
        this.manager = requestManager;
        this.requestContext = requestContext;
    }

    @Override
    public void fetchNotifications(final InboxResultListener<NotificationInboxStatus> resultListener) {
        Assert.notNull(resultListener, "ResultListener should not be null!");
        EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: resultListener %s", resultListener);

        if (requestContext.getAppLoginParameters() != null && requestContext.getAppLoginParameters().hasCredentials()) {
            handleFetchRequest(resultListener);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    resultListener.onError(new NotificationInboxException("AppLogin must be called before calling fetchNotifications!"));
                }
            });
        }
    }

    private void handleFetchRequest(final InboxResultListener<NotificationInboxStatus> resultListener) {
        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getRequestIdProvider())
                .url(INBOX_FETCH_V1)
                .headers(createBaseHeaders(requestContext.getConfig()))
                .method(RequestMethod.GET)
                .build();

        client.execute(model, new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, responseModel %s", id, responseModel);
                NotificationInboxStatus status = InboxParseUtils.parseNotificationInboxStatus(responseModel.getBody());
                NotificationInboxStatus resultStatus = new NotificationInboxStatus(cache.merge(status.getNotifications()), status.getBadgeCount());
                resultListener.onSuccess(resultStatus);
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, responseModel %s", id, responseModel);
                resultListener.onError(new MobileEngageException(responseModel));
            }

            @Override
            public void onError(String id, Exception cause) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, cause %s", id, cause);
                resultListener.onError(cause);
            }
        });
    }

    @Override
    public void resetBadgeCount(final ResetBadgeCountResultListener listener) {
        EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: resultListener %s", listener);
        if (requestContext.getAppLoginParameters() != null && requestContext.getAppLoginParameters().hasCredentials()) {
            handleResetRequest(listener);
        } else {
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onError(new NotificationInboxException("AppLogin must be called before calling fetchNotifications!"));
                    }
                });
            }
        }
    }

    @Override
    public String trackMessageOpen(Notification message) {
        EMSLogger.log(MobileEngageTopic.INBOX, "Argument: %s", message);

        Map<String, Object> payload = RequestPayloadUtils.createBasePayload(requestContext);
        payload.put("source", "inbox");
        payload.put("sid", message.getSid());
        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getRequestIdProvider())
                .url(RequestUrlUtils.createEventUrl_V2("message_open"))
                .payload(payload)
                .headers(RequestHeaderUtils.createBaseHeaders_V2(requestContext.getConfig()))
                .build();

        manager.submit(model);
        return model.getId();
    }

    @Override
    public void purgeNotificationCache() {

    }

    private void handleResetRequest(final ResetBadgeCountResultListener listener) {
        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getRequestIdProvider())
                .url(INBOX_RESET_BADGE_COUNT_V1)
                .headers(createBaseHeaders(requestContext.getConfig()))
                .method(RequestMethod.POST)
                .build();

        client.execute(model, new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, responseModel %s", id, responseModel);
                if (listener != null) {
                    listener.onSuccess();
                }
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, responseModel %s", id, responseModel);
                if (listener != null) {
                    listener.onError(new MobileEngageException(responseModel));
                }
            }

            @Override
            public void onError(String id, Exception cause) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, cause %s", id, cause);
                if (listener != null) {
                    listener.onError(cause);
                }
            }
        });
    }

    private Map<String, String> createBaseHeaders(MobileEngageConfig config) {
        Map<String, String> result = new HashMap<>();

        result.put("x-ems-me-hardware-id", new DeviceInfo(config.getApplication()).getHwid());
        result.put("x-ems-me-application-code", config.getApplicationCode());
        result.put("x-ems-me-contact-field-id", String.valueOf(requestContext.getAppLoginParameters().getContactFieldId()));
        result.put("x-ems-me-contact-field-value", requestContext.getAppLoginParameters().getContactFieldValue());

        result.putAll(RequestHeaderUtils.createDefaultHeaders(config));
        result.putAll(RequestHeaderUtils.createBaseHeaders_V2(config));

        return result;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

}
