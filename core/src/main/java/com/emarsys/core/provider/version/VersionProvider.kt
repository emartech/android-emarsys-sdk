package com.emarsys.core.provider.version

import com.emarsys.core.BuildConfig
import com.emarsys.core.Mockable

@Mockable
class VersionProvider {
    fun provideSdkVersion(): String = BuildConfig.VERSION_NAME
}
