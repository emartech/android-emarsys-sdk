package com.emarsys.mobileengage;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestIdProvider;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestContextTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_Config_ShouldNotBeNull() {
        new RequestContext(
                null,
                mock(DeviceInfo.class),
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class),
                mock(RequestIdProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_DeviceInfo_ShouldNotBeNull() {
        new RequestContext(
                mock(MobileEngageConfig.class),
                null,
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class),
                mock(RequestIdProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_AppLoginStorage_ShouldNotBeNull() {
        new RequestContext(
                mock(MobileEngageConfig.class),
                mock(DeviceInfo.class),
                null,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class),
                mock(RequestIdProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_MeIdStorage_ShouldNotBeNull() {
        new RequestContext(
                mock(MobileEngageConfig.class),
                mock(DeviceInfo.class),
                mock(AppLoginStorage.class),
                null,
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class),
                mock(RequestIdProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_MeIdSignatureStorage_ShouldNotBeNull() {
        new RequestContext(
                mock(MobileEngageConfig.class),
                mock(DeviceInfo.class),
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                null,
                mock(TimestampProvider.class),
                mock(RequestIdProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_TimestampProvider_ShouldNotBeNull() {
        new RequestContext(
                mock(MobileEngageConfig.class),
                mock(DeviceInfo.class),
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                null,
                mock(RequestIdProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_IdProvider_ShouldNotBeNull() {
        new RequestContext(
                mock(MobileEngageConfig.class),
                mock(DeviceInfo.class),
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class),
                null);
    }

    @Test
    public void testGetApplicationCode_shouldReturnConfigsApplicationCode() {
        String applicationCode = "applicationCode";
        MobileEngageConfig mockConfig = mock(MobileEngageConfig.class);
        when(mockConfig.getApplicationCode()).thenReturn(applicationCode);
        RequestContext underTest = new RequestContext(mockConfig,
                mock(DeviceInfo.class),
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class),
                mock(RequestIdProvider.class));

        Assert.assertEquals(applicationCode, underTest.getApplicationCode());
    }
}