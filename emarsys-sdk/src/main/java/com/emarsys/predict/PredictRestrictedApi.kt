package com.emarsys.predict

interface PredictRestrictedApi {

    fun setContact(contactFieldId: Int, contactFieldValue: String)
    fun clearContact()
}