package com.emarsys.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;

import com.emarsys.core.util.Assert;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DeviceInfo {
    static final String UNKNOWN_VERSION_NAME = "unknown";

    private final String hwid;
    private final String platform;
    private final String language;
    private final String timezone;
    private final String manufacturer;
    private final String model;
    private final String applicationVersion;
    private final String osVersion;
    private final DisplayMetrics displayMetrics;
    private final boolean isDebugMode;

    public DeviceInfo(Context context){
        Assert.notNull(context, "Context must not be null!");

        this.hwid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        this.manufacturer = Build.MANUFACTURER;

        this.model = Build.MODEL;

        this.platform = "android";

        this.language = Locale.getDefault().getLanguage();

        this.timezone = new SimpleDateFormat("Z", Locale.ENGLISH).format(Calendar.getInstance().getTime());

        this.applicationVersion = getApplicationVersion(context);

        this.osVersion = Build.VERSION.RELEASE;

        this.displayMetrics = Resources.getSystem().getDisplayMetrics();

        this.isDebugMode = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    }

    private String getApplicationVersion(Context context) {
        String version = null;
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        if (version == null) {
            version = UNKNOWN_VERSION_NAME;
        }
        return version;
    }

    public String getHwid() {
        return hwid;
    }

    public String getPlatform() {
        return platform;
    }

    public String getLanguage() {
        return language;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }
}
