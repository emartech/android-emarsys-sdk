package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener

interface RefreshTokenInternal {
    fun refreshContactToken(completionListener: CompletionListener?)
}