package com.emarsys.core.testUtil;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationTestUtils {

    public static Application applicationDebug() {
        Application applicationDebug = mock(Application.class, Mockito.RETURNS_DEEP_STUBS);
        ApplicationInfo info = new ApplicationInfo();
        info.flags = ApplicationInfo.FLAG_DEBUGGABLE;
        when(applicationDebug.getApplicationInfo()).thenReturn(info);
        return applicationDebug;
    }

    public static Application applicationRelease() {
        Application applicationRelease = mock(Application.class, Mockito.RETURNS_DEEP_STUBS);
        ApplicationInfo info = new ApplicationInfo();
        info.flags = 0;
        when(applicationRelease.getApplicationInfo()).thenReturn(info);
        return applicationRelease;
    }
}
