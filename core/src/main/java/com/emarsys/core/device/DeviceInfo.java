package com.emarsys.core.device;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import com.emarsys.core.provider.hardwareid.HardwareIdProvider;
import com.emarsys.core.util.Assert;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DeviceInfo {
    public static final String UNKNOWN_VERSION_NAME = "unknown";

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
    private final String sdkVersion;

    public DeviceInfo(Context context, HardwareIdProvider hardwareIdProvider, String sdkVersion, LanguageProvider languageProvider) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(hardwareIdProvider, "HardwareIdProvider must not be null!");
        Assert.notNull(sdkVersion, "SdkVersion must not be null!");
        Assert.notNull(languageProvider, "LanguageProvider must not be null!");

        this.hwid = hardwareIdProvider.provideHardwareId();

        this.manufacturer = Build.MANUFACTURER;

        this.model = Build.MODEL;

        this.platform = "android";

        this.language = languageProvider.provideLanguage(Locale.getDefault());

        this.timezone = new SimpleDateFormat("Z", Locale.ENGLISH).format(Calendar.getInstance().getTime());

        this.applicationVersion = getApplicationVersion(context);

        this.osVersion = Build.VERSION.RELEASE;

        this.displayMetrics = Resources.getSystem().getDisplayMetrics();

        this.isDebugMode = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));

        this.sdkVersion = sdkVersion;
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

    public String getSdkVersion() {
        return sdkVersion;
    }
}
