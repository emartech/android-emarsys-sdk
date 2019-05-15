package com.emarsys;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emarsys.config.EmarsysConfig;
import com.emarsys.core.Callable;
import com.emarsys.core.RunnerProxy;
import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.trigger.TriggerEvent;
import com.emarsys.core.database.trigger.TriggerType;
import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.di.DependencyInjection;
import com.emarsys.core.experimental.ExperimentalFeatures;
import com.emarsys.core.util.Assert;
import com.emarsys.di.DefaultEmarsysDependencyContainer;
import com.emarsys.di.EmarysDependencyContainer;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.api.EventHandler;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.iam.InAppInternal;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.predict.PredictInternal;
import com.emarsys.predict.api.model.CartItem;

import java.util.List;
import java.util.Map;

public class Emarsys {

    public static void setup(@NonNull EmarsysConfig config) {
        Assert.notNull(config, "Config must not be null!");

        for (FlipperFeature feature : config.getExperimentalFeatures()) {
            ExperimentalFeatures.enableFeature(feature);
        }

        DependencyInjection.setup(new DefaultEmarsysDependencyContainer(config));

        initializeInAppInternal(config);

        registerWatchDogs(config);

        registerDatabaseTriggers(config);

        initializeContact();
    }

    public static void setContact(@NonNull final String contactId) {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(contactId, "ContactId must not be null!");

                getMobileEngageInternal().setContact(contactId, null);

                getPredictInternal().setContact(contactId);
            }
        });

    }

    public static void setContact(
            @NonNull final String contactId,
            @NonNull final CompletionListener completionListener) {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(contactId, "ContactId must not be null!");
                Assert.notNull(completionListener, "CompletionListener must not be null!");

                getMobileEngageInternal().setContact(contactId, completionListener);
                getPredictInternal().setContact(contactId);
            }
        });
    }

    public static void clearContact() {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                getMobileEngageInternal().clearContact(null);
                getPredictInternal().clearContact();
            }
        });
    }

    public static void clearContact(@NonNull final CompletionListener completionListener) {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(completionListener, "CompletionListener must not be null!");

                getMobileEngageInternal().clearContact(completionListener);
                getPredictInternal().clearContact();
            }
        });

    }

    public static void trackDeepLink(@NonNull final Activity activity,
                                     @NonNull final Intent intent) {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(activity, "Activity must not be null!");
                Assert.notNull(intent, "Intent must not be null!");

                getDeepLinkInternal().trackDeepLinkOpen(activity, intent, null);
            }
        });
    }

    public static void trackDeepLink(@NonNull final Activity activity,
                                     @NonNull final Intent intent,
                                     @NonNull final CompletionListener completionListener) {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(activity, "Activity must not be null!");
                Assert.notNull(intent, "Intent must not be null!");
                Assert.notNull(completionListener, "CompletionListener must not be null!");

                getDeepLinkInternal().trackDeepLinkOpen(activity, intent, completionListener);
            }
        });
    }

    public static void trackCustomEvent(
            @NonNull final String eventName,
            @Nullable final Map<String, String> eventAttributes) {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(eventName, "EventName must not be null!");

                getMobileEngageInternal().trackCustomEvent(eventName, eventAttributes, null);
            }
        });
    }

    public static void trackCustomEvent(
            @NonNull final String eventName,
            @Nullable final Map<String, String> eventAttributes,
            @NonNull final CompletionListener completionListener) {
        getRunnerProxy().logException(new Runnable() {
            @Override
            public void run() {
                Assert.notNull(eventName, "EventName must not be null!");
                Assert.notNull(completionListener, "CompletionListener must not be null!");

                getMobileEngageInternal().trackCustomEvent(eventName, eventAttributes, completionListener);
            }
        });
    }

    public static class Push {

        public static void trackMessageOpen(@NonNull final Intent intent) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(intent, "Intent must not be null!");

                    getMobileEngageInternal().trackMessageOpen(intent, null);
                }
            });
        }

        public static void trackMessageOpen(
                @NonNull final Intent intent,
                @NonNull final CompletionListener completionListener) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(intent, "Intent must not be null!");
                    Assert.notNull(completionListener, "CompletionListener must not be null!");

                    getMobileEngageInternal().trackMessageOpen(intent, completionListener);
                }
            });
        }

        public static void setPushToken(@NonNull final String pushToken) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(pushToken, "PushToken must not be null!");

                    getMobileEngageInternal().setPushToken(pushToken, null);
                }
            });
        }

        public static void setPushToken(
                @NonNull final String pushToken,
                @NonNull final CompletionListener completionListener) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(pushToken, "PushToken must not be null!");
                    Assert.notNull(completionListener, "CompletionListener must not be null!");

                    getMobileEngageInternal().setPushToken(pushToken, completionListener);
                }
            });
        }

        public static void clearPushToken() {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    getMobileEngageInternal().clearPushToken(null);
                }
            });
        }

        public static void clearPushToken(final CompletionListener completionListener) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(completionListener, "CompletionListener must not be null!");

                    getMobileEngageInternal().clearPushToken(completionListener);
                }
            });
        }
    }

    public static class Inbox {

        public static void fetchNotifications(
                @NonNull final ResultListener<Try<NotificationInboxStatus>> resultListener) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(resultListener, "ResultListener must not be null!");

                    getInboxInternal().fetchNotifications(resultListener);
                }
            });
        }

        public static void trackNotificationOpen(@NonNull final Notification notification) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(notification, "Notification must not be null!");

                    getInboxInternal().trackNotificationOpen(notification, null);
                }
            });
        }

        public static void trackNotificationOpen(
                @NonNull final Notification notification,
                @NonNull final CompletionListener completionListener) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(notification, "Notification must not be null!");
                    Assert.notNull(completionListener, "CompletionListener must not be null!");

                    getInboxInternal().trackNotificationOpen(notification, completionListener);
                }
            });
        }

        public static void resetBadgeCount() {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    getInboxInternal().resetBadgeCount(null);
                }
            });
        }

        public static void resetBadgeCount(@NonNull final CompletionListener completionListener) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(completionListener, "CompletionListener must not be null!");

                    getInboxInternal().resetBadgeCount(completionListener);
                }
            });
        }
    }

    public static class InApp {

        public static void pause() {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    getInAppInternal().pause();
                }
            });
        }

        public static void resume() {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    getInAppInternal().resume();
                }
            });
        }

        public static boolean isPaused() {
            return getRunnerProxy().logException(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return getInAppInternal().isPaused();
                }
            });
        }

        public static void setEventHandler(@NonNull final EventHandler eventHandler) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(eventHandler, "EventHandler must not be null!");

                    getInAppInternal().setEventHandler(eventHandler);
                }
            });
        }
    }

    static class Predict {

        public static void trackCart(@NonNull final List<CartItem> items) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(items, "Items must not be null!");
                    Assert.elementsNotNull(items, "Item elements must not be null!");

                    getPredictInternal().trackCart(items);
                }
            });
        }

        public static void trackPurchase(@NonNull final String orderId,
                                         @NonNull final List<CartItem> items) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(orderId, "OrderId must not be null!");
                    Assert.notNull(items, "Items must not be null!");
                    Assert.elementsNotNull(items, "Item elements must not be null!");

                    getPredictInternal().trackPurchase(orderId, items);
                }
            });
        }

        public static void trackItemView(@NonNull final String itemId) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(itemId, "ItemId must not be null!");

                    getPredictInternal().trackItemView(itemId);
                }
            });
        }

        public static void trackCategoryView(@NonNull final String categoryPath) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(categoryPath, "CategoryPath must not be null!");

                    getPredictInternal().trackCategoryView(categoryPath);
                }
            });
        }

        public static void trackSearchTerm(@NonNull final String searchTerm) {
            getRunnerProxy().logException(new Runnable() {
                @Override
                public void run() {
                    Assert.notNull(searchTerm, "SearchTerm must not be null!");

                    getPredictInternal().trackSearchTerm(searchTerm);
                }
            });
        }
    }

    private static EmarysDependencyContainer getContainer() {
        return DependencyInjection.getContainer();
    }

    private static MobileEngageInternal getMobileEngageInternal() {
        return getContainer().getMobileEngageInternal();
    }

    private static InboxInternal getInboxInternal() {
        return getContainer().getInboxInternal();
    }

    private static InAppInternal getInAppInternal() {
        return getContainer().getInAppInternal();
    }

    private static DeepLinkInternal getDeepLinkInternal() {
        return getContainer().getDeepLinkInternal();
    }

    private static PredictInternal getPredictInternal() {
        return getContainer().getPredictInternal();
    }

    private static RunnerProxy getRunnerProxy() {
        return getContainer().getRunnerProxy();
    }

    private static void initializeInAppInternal(@NonNull EmarsysConfig config) {
        EventHandler inAppEventHandler = config.getInAppEventHandler();

        if (inAppEventHandler != null) {
            getInAppInternal().setEventHandler(inAppEventHandler);
        }
    }

    private static void registerWatchDogs(EmarsysConfig config) {
        config.getApplication().registerActivityLifecycleCallbacks(getContainer().getActivityLifecycleWatchdog());
        config.getApplication().registerActivityLifecycleCallbacks(getContainer().getCurrentActivityWatchdog());
    }

    private static void registerDatabaseTriggers(EmarsysConfig config) {
        boolean isPredictEnabled = config.getPredictMerchantId() != null;

        if (isPredictEnabled) {
            getContainer().getCoreSQLiteDatabase().registerTrigger(
                    DatabaseContract.SHARD_TABLE_NAME,
                    TriggerType.AFTER,
                    TriggerEvent.INSERT,
                    getContainer().getPredictShardTrigger());
        }

        getContainer().getCoreSQLiteDatabase().registerTrigger(
                DatabaseContract.SHARD_TABLE_NAME,
                TriggerType.AFTER,
                TriggerEvent.INSERT,
                getContainer().getLogShardTrigger());
    }

    private static void initializeContact() {
        Integer deviceInfoHash = getContainer().getDeviceInfoHashStorage().get();
        String contactToken = getContainer().getContactTokenStorage().get();
        String contactFieldValue = getContainer().getContactFieldValueStorage().get();
        String clientState = getContainer().getClientStateStorage().get();
        DeviceInfo deviceInfo = getContainer().getDeviceInfo();

        if (contactToken == null && contactFieldValue == null) {
            if (clientState == null || deviceInfoHash != null && !deviceInfoHash.equals(deviceInfo.getHash())) {
                getMobileEngageInternal().trackDeviceInfo();
            }
            getMobileEngageInternal().setContact(null, null);
        }
    }
}