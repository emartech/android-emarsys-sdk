package com.emarsys.service;

import android.content.Context;

import com.emarsys.core.Callable;
import com.emarsys.core.di.DependencyInjection;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer;
import com.emarsys.mobileengage.service.MessagingServiceUtils;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class EmarsysMessagingServiceUtils {
    public static String MESSAGE_FILTER = MessagingServiceUtils.MESSAGE_FILTER;

    public static boolean handleMessage(final Context context, final RemoteMessage remoteMessage) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(remoteMessage, "RemoteMessage must not be null!");

        final MobileEngageDependencyContainer container = DependencyInjection.getContainer();

        return container.getRunnerProxy().logException(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return MessagingServiceUtils.handleMessage(
                        context,
                        remoteMessage,
                        container.getDeviceInfo(),
                        container.getNotificationCache(),
                        container.getTimestampProvider(),
                        container.getFileDownloader());
            }
        });
    }

    public static boolean isMobileEngageMessage(Map<String, String> remoteMessageData) {
        return MessagingServiceUtils.isMobileEngageMessage(remoteMessageData);
    }
}
