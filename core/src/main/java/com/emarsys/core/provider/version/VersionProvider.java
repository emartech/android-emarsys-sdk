package com.emarsys.core.provider.version;

import com.emarsys.core.BuildConfig;
import com.emarsys.core.Mockable;

@Mockable
public class VersionProvider {

    public String provideSdkVersion() {
        return BuildConfig.VERSION_NAME;
    }
}
