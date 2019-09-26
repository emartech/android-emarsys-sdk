package com.emarsys.config

import com.emarsys.core.api.result.CompletionListener

interface ConfigInternal {

    val contactFieldId: Int

    val applicationCode: String?

    val merchantId: String?

    fun changeApplicationCode(applicationCode: String?, completionListener: CompletionListener?)

    fun changeMerchantId(merchantId: String?)
}
