package com.emarsys.mobileengage.session

import com.emarsys.core.Mockable
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.session.Session
import com.emarsys.mobileengage.event.EventServiceInternal

@Mockable
class MobileEngageSession(private val timestampProvider: TimestampProvider,
                          private val uuidProvider: UUIDProvider,
                          private val eventServiceInternal: EventServiceInternal,
                          private val sessionIdHolder: SessionIdHolder) : Session {

    private var sessionStart: Long? = null

    override fun startSession() {
        sessionIdHolder.sessionId = uuidProvider.provideId()
        sessionStart = timestampProvider.provideTimestamp()

        eventServiceInternal.trackInternalCustomEventAsync("session:start", null, null)
    }

    override fun endSession() {
        if (sessionIdHolder.sessionId != null && sessionStart != null) {
            val sessionEnd = (timestampProvider.provideTimestamp() - sessionStart!!).toString()
            val attributes = mapOf(
                    "duration" to sessionEnd
            )
            eventServiceInternal.trackInternalCustomEventAsync("session:end", attributes, null)

            sessionIdHolder.sessionId = null
            sessionStart = null
        } else {
            throw IllegalStateException("StartSession has to be called first!")
        }
    }
}
