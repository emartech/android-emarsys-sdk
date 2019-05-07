package com.emarsys.core.provider.version;

import com.emarsys.core.BuildConfig;

public class VersionProvider {

    public String provideSdkVersion() {
        return BuildConfig.VERSION_NAME;
    }
}
