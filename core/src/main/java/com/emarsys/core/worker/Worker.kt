package com.emarsys.core.worker

interface Worker : Lockable {
    fun run()
}