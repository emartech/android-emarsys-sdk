package com.emarsys.core.activity

class ActivityLifecyclePriorities {
    companion object {
        const val CREATE_PRIORITY = 0
        const val RESUME_PRIORITY = 1000

        const val DEEP_LINK_ACTION_PRIORITY = 0
        const val FETCH_GEOFENCE_ACTION_PRIORITY = 0
        const val FETCH_REMOTE_CONFIG_ACTION_PRIORITY = 100
        const val PUSH_TO_INAPP_ACTION_PRIORITY = 900
        const val APP_START_ACTION_PRIORITY = 200
        const val DEVICE_INFO_START_ACTION_PRIORITY = 300
    }
}