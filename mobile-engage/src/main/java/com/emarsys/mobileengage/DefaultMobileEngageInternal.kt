package com.emarsys.mobileengage

import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.RequestManager
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.mobileengage.session.MobileEngageSession
import com.emarsys.mobileengage.session.SessionIdHolder

@Mockable
class DefaultMobileEngageInternal(
    private val requestManager: RequestManager,
    private val requestModelFactory: MobileEngageRequestModelFactory,
    private val requestContext: MobileEngageRequestContext,
    private val session: MobileEngageSession,
    private val sessionIdHolder: SessionIdHolder
) : MobileEngageInternal {

    override fun setContact(
        contactFieldId: Int?,
        contactFieldValue: String?,
        completionListener: CompletionListener?
    ) {
        val shouldRestartSession = requestContext.contactFieldValue != contactFieldValue
        doSetContact(contactFieldId, contactFieldValue, completionListener = completionListener)

        if (shouldRestartSession) {
            if (!sessionIdHolder.sessionId.isNullOrEmpty()) {
                session.endSession {
                    if (it != null) {
                        Logger.error(CrashLog(it))
                    }
                }
            }
            session.startSession {
                if (it != null) {
                    Logger.error(CrashLog(it))
                }
            }
        }
    }

    override fun setAuthenticatedContact(
        contactFieldId: Int,
        openIdToken: String,
        completionListener: CompletionListener?
    ) {
        val shouldRestartSession = requestContext.openIdToken != openIdToken
        doSetContact(contactFieldId, null, openIdToken, completionListener)

        if (shouldRestartSession) {
            if (!sessionIdHolder.sessionId.isNullOrEmpty()) {
                session.endSession {
                    if (it != null) {
                        Logger.error(CrashLog(it))
                    }
                }
            }
            session.startSession {
                if (it != null) {
                    Logger.error(CrashLog(it))
                }
            }
        }
    }

    internal fun doSetContact(
        contactFieldId: Int?,
        contactFieldValue: String?,
        idToken: String? = null,
        completionListener: CompletionListener?
    ) {
        requestContext.contactFieldId = contactFieldId
        requestContext.contactFieldValue = contactFieldValue
        requestContext.openIdToken = idToken
        try {
            val requestModel =
                requestModelFactory.createSetContactRequest(contactFieldId, contactFieldValue)
            requestManager.submit(requestModel, completionListener)
        } catch (e: IllegalArgumentException) {
            completionListener?.onCompleted(e)
        }
    }

    override fun clearContact(completionListener: CompletionListener?) {
        if (!sessionIdHolder.sessionId.isNullOrEmpty()) {
            session.endSession {
                if (it != null) {
                    Logger.error(CrashLog(it))
                }
                doClearContact(completionListener)
            }
        } else {
            doClearContact(completionListener)
        }
    }

    internal fun doClearContact(completionListener: CompletionListener?) {
        resetContext()

        doSetContact(null, null, null) { setContactThrowable ->
            completionListener?.onCompleted(setContactThrowable)
            session.startSession { sessionStartThrowable ->
                if (sessionStartThrowable != null) {
                    Logger.error(CrashLog(sessionStartThrowable))
                }
            }
        }
    }

    fun resetContext() {
        requestContext.refreshTokenStorage.remove()
        requestContext.contactTokenStorage.remove()
        requestContext.pushTokenStorage.remove()
        requestContext.openIdToken = null
        requestContext.contactFieldValue = null
        requestContext.contactFieldId = null
    }
}