package com.emarsys.config

import com.emarsys.core.Mockable
import com.emarsys.core.api.notification.NotificationSettings
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.di.getDependency
@Mockable
class Config : ConfigApi {
    override val contactFieldId: Int
        get() = getDependency<ConfigInternal>().contactFieldId

    override val applicationCode: String?
        get() = getDependency<ConfigInternal>().applicationCode

    override val merchantId: String?
        get() = getDependency<ConfigInternal>().merchantId
    override val hardwareId: String
        get() = getDependency<ConfigInternal>().hardwareId
    override val language: String
        get() = getDependency<ConfigInternal>().language
    override val notificationSettings: NotificationSettings
        get() = getDependency<ConfigInternal>().notificationSettings

    override fun changeApplicationCode(applicationCode: String?) {
        changeApplicationCode(applicationCode, contactFieldId)
    }

    override fun changeApplicationCode(applicationCode: String?, completionListener: CompletionListener?) {
        changeApplicationCode(applicationCode, contactFieldId, completionListener)
    }

    override fun changeApplicationCode(applicationCode: String?, completionListener: (throwable: Throwable?) -> Unit) {
        changeApplicationCode(applicationCode, CompletionListener { completionListener(it) })
    }

    override fun changeApplicationCode(applicationCode: String?, contactFieldId: Int) {
        changeApplicationCode(applicationCode, contactFieldId, null)
    }

    override fun changeApplicationCode(applicationCode: String?, contactFieldId: Int, completionListener: CompletionListener?) {
        getDependency<ConfigInternal>()
                .changeApplicationCode(applicationCode, contactFieldId, completionListener)
    }

    override fun changeApplicationCode(applicationCode: String?, contactFieldId: Int, completionListener: (throwable: Throwable?) -> Unit) {
        changeApplicationCode(applicationCode, contactFieldId, CompletionListener { completionListener(it) })
    }

    override fun changeMerchantId(merchantId: String?) {
        getDependency<ConfigInternal>()
                .changeMerchantId(merchantId)
    }
}
