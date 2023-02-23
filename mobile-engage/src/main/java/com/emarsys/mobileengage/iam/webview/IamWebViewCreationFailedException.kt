package com.emarsys.mobileengage.iam.webview

data class IamWebViewCreationFailedException(val statusMessage: String = "IamWebView creation failed!") : Exception(statusMessage)