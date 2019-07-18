package com.emarsys.mobileengage.inbox;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.api.ResponseErrorException;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory;

public class DefaultInboxInternal implements InboxInternal {

    private Handler handler;
    private NotificationCache cache;
    private RequestManager manager;
    private RequestContext requestContext;
    private MobileEngageRequestModelFactory requestModelFactory;

    public DefaultInboxInternal(
            RequestManager requestManager,
            RequestContext requestContext,
            MobileEngageRequestModelFactory requestModelFactory) {
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");
        Assert.notNull(requestModelFactory, "RequestModelFactory must not be null!");

        this.handler = new Handler(Looper.getMainLooper());
        this.cache = new NotificationCache();
        this.manager = requestManager;
        this.requestContext = requestContext;
        this.requestModelFactory = requestModelFactory;
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
        RequestModel model = requestModelFactory.createFetchNotificationsRequest();

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

        RequestModel model = requestModelFactory.createTrackNotificationOpenRequest(notification.getSid());

        manager.submit(model, completionListener);
    }

    private void handleResetRequest(final CompletionListener resultListener) {
        RequestModel model = requestModelFactory.createResetBadgeCountRequest();

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

    public RequestContext getRequestContext() {
        return requestContext;
    }

}
