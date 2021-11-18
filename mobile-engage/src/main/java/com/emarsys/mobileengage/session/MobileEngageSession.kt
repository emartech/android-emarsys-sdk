package com.emarsys.mobileengage.session

import com.emarsys.core.Mockable
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.session.Session
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import com.emarsys.mobileengage.event.EventServiceInternal

@Mockable
class MobileEngageSession(private val timestampProvider: TimestampProvider,
                          private val uuidProvider: UUIDProvider,
                          private val eventServiceInternal: EventServiceInternal,
                          private val sessionIdHolder: SessionIdHolder) : Session {

    private var sessionStart: Long? = null

    override fun startSession(completionListener: CompletionListener) {
        sessionIdHolder.sessionId = uuidProvider.provideId()
        sessionStart = timestampProvider.provideTimestamp()

        eventServiceInternal.trackInternalCustomEventAsync("session:start", null, completionListener)
    }

    override fun endSession(completionListener: CompletionListener) {
        if (sessionIdHolder.sessionId != null && sessionStart != null) {
            val sessionEnd = (timestampProvider.provideTimestamp() - sessionStart!!).toString()
            val attributes = mapOf(
                    "duration" to sessionEnd
            )
            eventServiceInternal.trackInternalCustomEventAsync("session:end", attributes, completionListener)

            sessionIdHolder.sessionId = null
            sessionStart = null
        } else {
            throw IllegalStateException("StartSession has to be called first!")
        }
    }
}
