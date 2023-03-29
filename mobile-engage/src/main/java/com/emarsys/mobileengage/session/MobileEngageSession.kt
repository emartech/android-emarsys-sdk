package com.emarsys.mobileengage.session

import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.session.Session
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.StatusLog
import com.emarsys.mobileengage.event.EventServiceInternal

@Mockable
class MobileEngageSession(private val timestampProvider: TimestampProvider,
                          private val uuidProvider: UUIDProvider,
                          private val eventServiceInternal: EventServiceInternal,
                          private val sessionIdHolder: SessionIdHolder,
                          private val contactTokenStorage: Storage<String?>) : Session {

    private var sessionStart: Long? = null

    override fun startSession(completionListener: CompletionListener) {
        if (!contactTokenStorage.get().isNullOrEmpty()) {
            sessionIdHolder.sessionId = uuidProvider.provideId()
            sessionStart = timestampProvider.provideTimestamp()
            eventServiceInternal.trackInternalCustomEventAsync("session:start", null, completionListener)
        }
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
            if (!contactTokenStorage.get().isNullOrEmpty()) {
                Logger.info(StatusLog(this::class.java, "endSession", parameters = null, status = mapOf("cause" to "StartSession has to be called first!")))
            }
        }
    }
}
