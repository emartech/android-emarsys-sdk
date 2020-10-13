package com.emarsys.sample.prefs

import com.chibatching.kotpref.KotprefModel

object Cache : KotprefModel() {
    var contactFieldValue by nullableStringPref()
    var contactFieldId by intPref()
    var applicationCode by nullableStringPref()
    var merchantId by nullableStringPref()
}