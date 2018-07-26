package com.emarsys.mobileengage.di;

import android.os.Handler;

import com.emarsys.core.activity.ActivityLifecycleWatchdog;
import com.emarsys.mobileengage.MobileEngageCoreCompletionHandler;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.iam.InAppPresenter;
import com.emarsys.mobileengage.inbox.InboxInternal;

public interface DependencyContainer {

    MobileEngageInternal getMobileEngageInternal();

    InboxInternal getInboxInternal();

    DeepLinkInternal getDeepLinkInternal();

    MobileEngageCoreCompletionHandler getCoreCompletionHandler();

    Handler getCoreSdkHandler();

    RequestContext getRequestContext();

    ActivityLifecycleWatchdog getActivityLifecycleWatchdog();

    InAppPresenter getInAppPresenter();
}
