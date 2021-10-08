package com.emarsys.core.worker

interface Lockable {
    fun lock()
    fun unlock()
    val isLocked: Boolean
}