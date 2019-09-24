package com.emarsys.config

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.push.PushTokenProvider

class DefaultConfigInternal(private val mobileEngageRequestContext: MobileEngageRequestContext,
                            private val mobileEngageInternal: MobileEngageInternal,
                            private val pushInternal: PushInternal,
                            private val pushTokenProvider: PushTokenProvider) : ConfigInternal {
    override val applicationCode: String?
        get() = mobileEngageRequestContext.applicationCode

    override val contactFieldId: Int
        get() = mobileEngageRequestContext.contactFieldId


    override fun changeApplicationCode(applicationCode: String?, completionListener: CompletionListener?) {
        val originalContactFieldValue = mobileEngageRequestContext.contactFieldValueStorage.get()
        mobileEngageInternal.clearContact {
            pushInternal.setPushToken(pushTokenProvider.providePushToken()) {
                mobileEngageInternal.setContact(originalContactFieldValue, completionListener)
            }
        }
    }

    override fun changeMerchantId(merchantId: String?) {
    }
}
