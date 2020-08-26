package com.emarsys.mobileengage.iam.jsbridge

import org.json.JSONObject

typealias OnAppEventListener = (property: String?, json: JSONObject) -> Unit
