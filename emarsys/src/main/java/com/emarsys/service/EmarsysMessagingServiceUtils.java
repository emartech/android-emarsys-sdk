package com.emarsys.service;

import android.content.Context;

import com.emarsys.core.di.DependencyContainer;
import com.emarsys.core.di.DependencyInjection;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer;
import com.emarsys.mobileengage.service.MessagingServiceUtils;
import com.google.firebase.messaging.RemoteMessage;

public class EmarsysMessagingServiceUtils {

    public static boolean handleMessage(Context context, RemoteMessage remoteMessage) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(remoteMessage, "RemoteMessage must not be null!");

        DependencyContainer container = DependencyInjection.<MobileEngageDependencyContainer>getContainer();

        return MessagingServiceUtils.handleMessage(
                context,
                remoteMessage,
                container.getDeviceInfo());
    }

}
