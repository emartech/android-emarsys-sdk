package com.emarsys.core.device;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import com.emarsys.core.provider.hardwareid.HardwareIdProvider;
import com.emarsys.core.provider.version.VersionProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.SystemUtils;

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
    private boolean kotlinEnabled;

    public DeviceInfo(Context context, HardwareIdProvider hardwareIdProvider, VersionProvider versionProvider, LanguageProvider languageProvider) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(hardwareIdProvider, "HardwareIdProvider must not be null!");
        Assert.notNull(versionProvider, "VersionProvider must not be null!");
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

        this.sdkVersion = versionProvider.provideSdkVersion();

        this.kotlinEnabled = SystemUtils.isKotlinEnabled();
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

    public Integer getHash() {
        return hashCode();
    }

    public boolean isKotlinEnabled() {
        return kotlinEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceInfo that = (DeviceInfo) o;

        if (isDebugMode != that.isDebugMode) return false;
        if (kotlinEnabled != that.kotlinEnabled) return false;
        if (hwid != null ? !hwid.equals(that.hwid) : that.hwid != null) return false;
        if (platform != null ? !platform.equals(that.platform) : that.platform != null)
            return false;
        if (language != null ? !language.equals(that.language) : that.language != null)
            return false;
        if (timezone != null ? !timezone.equals(that.timezone) : that.timezone != null)
            return false;
        if (manufacturer != null ? !manufacturer.equals(that.manufacturer) : that.manufacturer != null)
            return false;
        if (model != null ? !model.equals(that.model) : that.model != null) return false;
        if (applicationVersion != null ? !applicationVersion.equals(that.applicationVersion) : that.applicationVersion != null)
            return false;
        if (osVersion != null ? !osVersion.equals(that.osVersion) : that.osVersion != null)
            return false;
        if (displayMetrics != null ? !displayMetrics.equals(that.displayMetrics) : that.displayMetrics != null)
            return false;
        return sdkVersion != null ? sdkVersion.equals(that.sdkVersion) : that.sdkVersion == null;
    }

    @Override
    public int hashCode() {
        int result = hwid != null ? hwid.hashCode() : 0;
        result = 31 * result + (platform != null ? platform.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (timezone != null ? timezone.hashCode() : 0);
        result = 31 * result + (manufacturer != null ? manufacturer.hashCode() : 0);
        result = 31 * result + (model != null ? model.hashCode() : 0);
        result = 31 * result + (applicationVersion != null ? applicationVersion.hashCode() : 0);
        result = 31 * result + (osVersion != null ? osVersion.hashCode() : 0);
        result = 31 * result + (displayMetrics != null ? displayMetrics.hashCode() : 0);
        result = 31 * result + (isDebugMode ? 1 : 0);
        result = 31 * result + (sdkVersion != null ? sdkVersion.hashCode() : 0);
        result = 31 * result + (kotlinEnabled ? 1 : 0);
        return result;
    }
}
