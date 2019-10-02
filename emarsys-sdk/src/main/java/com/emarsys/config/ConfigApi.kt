package com.emarsys.config

import com.emarsys.core.api.result.CompletionListener

interface ConfigApi {

    val contactFieldId: Int

    val applicationCode: String?

    val merchantId: String?

    fun changeApplicationCode(applicationCode: String?, completionListener: CompletionListener?)

    fun changeApplicationCode(applicationCode: String?, completionListener: (throwable: Throwable?) -> Unit)

    fun changeMerchantId(merchantId: String?)
}
