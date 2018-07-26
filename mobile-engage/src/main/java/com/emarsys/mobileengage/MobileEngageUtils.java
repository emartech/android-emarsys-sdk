package com.emarsys.mobileengage;

import android.support.test.espresso.IdlingResource;

import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.util.MobileEngageIdlingResource;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

public class MobileEngageUtils {

    static MobileEngageIdlingResource idlingResource;
    static boolean idlingResourceEnabled;

    public static void setup(MobileEngageConfig config) {
        idlingResourceEnabled = config.isIdlingResourceEnabled();
        idlingResource = idlingResourceEnabled ? new MobileEngageIdlingResource("mobile-engage-idling-resource") : null;
    }

    public static IdlingResource getIdlingResource() {
        return idlingResource;
    }

    public static void incrementIdlingResource() {
        if(idlingResourceEnabled){
            EMSLogger.log(MobileEngageTopic.IDLING_RESOURCE, "Incremented");
            idlingResource.increment();
        }
    }

    public static void decrementIdlingResource() {
        if(idlingResourceEnabled){
            EMSLogger.log(MobileEngageTopic.IDLING_RESOURCE, "Decremented");
            idlingResource.decrement();
        }
    }
}
