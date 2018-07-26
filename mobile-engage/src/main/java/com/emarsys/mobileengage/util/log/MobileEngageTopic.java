package com.emarsys.mobileengage.util.log;

import com.emarsys.core.util.log.LogTopic;

public enum MobileEngageTopic implements LogTopic {
    /**
     * Topic for Mobile Engage requests
     */
    MOBILE_ENGAGE("ems_mobile_engage"),

    /**
     * Topic for Notification Inbox requests
     */
    INBOX("ems_inbox"),

    /**
     * Topic for In App Message
     */
    IN_APP_MESSAGE("ems_in_app_message"),

    /**
     * Topic for receiving push tokens and getting push notifications
     */
    PUSH("ems_push"),
    /**
     * Topic for observing Mobile Engage IdlingResource state changes
     */
    IDLING_RESOURCE("ems_idling_resource");

    private String tag;

    MobileEngageTopic(String tag) {
        this.tag = tag;
    }

    @Override
    public String getTag() {
        return tag;
    }
}
