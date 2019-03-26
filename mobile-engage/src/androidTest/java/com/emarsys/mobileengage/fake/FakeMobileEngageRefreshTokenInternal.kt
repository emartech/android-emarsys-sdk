package com.emarsys.mobileengage.fake

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.RefreshTokenInternal

class FakeMobileEngageRefreshTokenInternal(private val success: Boolean = false) : RefreshTokenInternal {

    override fun refreshContactToken(completionListener: CompletionListener) {
        if (success) {
            completionListener.onCompleted(null)
        } else {
            completionListener.onCompleted(Exception())
        }
    }
}
