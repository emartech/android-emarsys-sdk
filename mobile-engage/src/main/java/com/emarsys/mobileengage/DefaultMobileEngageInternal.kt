package com.emarsys.mobileengage

import com.emarsys.core.Mockable
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.request.RequestManager
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
                session.endSession()
            }
            session.startSession()
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
                session.endSession()
            }
            session.startSession()
        }
    }

    private fun doSetContact(
        contactFieldId: Int?,
        contactFieldValue: String?,
        idToken: String? = null,
        completionListener: CompletionListener?
    ) {
        requestContext.contactFieldId = contactFieldId
        requestContext.contactFieldValue = contactFieldValue
        requestContext.openIdToken = idToken
        val requestModel =
            requestModelFactory.createSetContactRequest(contactFieldId, contactFieldValue)
        requestManager.submit(requestModel, completionListener)
    }

    override fun clearContact(completionListener: CompletionListener?) {
        resetContext()
        if (!sessionIdHolder.sessionId.isNullOrEmpty()) {
            session.endSession()
        }
        doSetContact(null, null, completionListener = completionListener)

        session.startSession()
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