package com.emarsys.core.resource;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.emarsys.core.util.Assert;

public class MetaDataReader {

    public int getInt(Context context, String key) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(key, "Key must not be null!");

        int result = 0;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData.containsKey(key)) {
                result = ai.metaData.getInt(key);
            }
        } catch (PackageManager.NameNotFoundException ignore) {
        }
        return result;
    }

    public int getInt(Context context, String key, int defaultValue) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(key, "Key must not be null!");

        int result = getInt(context, key);

        return result == 0 ? defaultValue : result;
    }

}
