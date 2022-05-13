package com.emarsys.sample.inapp

import java.util.*

class UuidProvider {

    companion object {
        fun provide(): UUID {
            return UUID.randomUUID()
        }
    }
}