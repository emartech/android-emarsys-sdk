package com.emarsys.core.session

interface Session {
    val sessionId: String?
    fun startSession();
    fun endSession();
}