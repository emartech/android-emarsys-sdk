package com.emarsys.mobileengage.iam

import android.app.Activity
import com.emarsys.core.activity.ActivityLifecycleAction
import com.emarsys.core.activity.ActivityLifecyclePriorities
import com.emarsys.core.provider.timestamp.TimestampProvider

class PushToInAppAction(
    private val overlayInAppPresenter: OverlayInAppPresenter,
    private val campaignId: String,
    private val html: String,
    private val sid: String?,
    private val url: String?,
    private val timestampProvider: TimestampProvider,
    override val priority: Int = ActivityLifecyclePriorities.PUSH_TO_INAPP_ACTION_PRIORITY,
    override val repeatable: Boolean = false,
    override val triggeringLifecycle: ActivityLifecycleAction.ActivityLifecycle = ActivityLifecycleAction.ActivityLifecycle.RESUME
) : ActivityLifecycleAction {
    override fun execute(activity: Activity?) {
        overlayInAppPresenter.present(
            campaignId, sid, url, null, timestampProvider.provideTimestamp(), html, null
        )
    }
}