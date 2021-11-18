package com.emarsys.core.session

import com.emarsys.core.api.result.CompletionListener

interface Session {
    fun startSession(completionListener: CompletionListener)
    fun endSession(completionListener: CompletionListener)
}