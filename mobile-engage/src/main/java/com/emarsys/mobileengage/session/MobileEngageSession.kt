package com.emarsys.mobileengage.session

import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.session.Session
import com.emarsys.mobileengage.event.EventServiceInternal

class MobileEngageSession(private val timestampProvider: TimestampProvider,
                          private val uuidProvider: UUIDProvider,
                          private val eventServiceInternal: EventServiceInternal) : Session {

    private var sessionStart: Long? = null

    override var sessionId: String? = null
        private set

    override fun startSession() {
        sessionId = uuidProvider.provideId()
        sessionStart = timestampProvider.provideTimestamp()

        eventServiceInternal.trackInternalCustomEventAsync("session:start", null, null)
    }

    override fun endSession() {
        if (sessionId != null) {
            val sessionEnd = (timestampProvider.provideTimestamp() - sessionStart!!).toString()
            val attributes = mapOf(
                    "duration" to sessionEnd
            )
            eventServiceInternal.trackInternalCustomEventAsync("session:end", attributes, null)

            sessionId = null
        } else {
            throw IllegalStateException("StartSession has to be called first!")
        }
    }
}
