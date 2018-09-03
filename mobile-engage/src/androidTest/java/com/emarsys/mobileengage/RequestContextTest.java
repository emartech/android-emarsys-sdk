package com.emarsys.mobileengage;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.test.util.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;

public class RequestContextTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationCode_ShouldNotBeNull() {
        new RequestContext(
                null,
                "",
                mock(DeviceInfo.class),
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class),
                mock(UUIDProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationPassword_ShouldNotBeNull() {
        new RequestContext(
                "",
                null,
                mock(DeviceInfo.class),
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class),
                mock(UUIDProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_DeviceInfo_ShouldNotBeNull() {
        new RequestContext(
                "",
                "",
                null,
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class),
                mock(UUIDProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_AppLoginStorage_ShouldNotBeNull() {
        new RequestContext(
                "",
                "",
                mock(DeviceInfo.class),
                null,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class),
                mock(UUIDProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_MeIdStorage_ShouldNotBeNull() {
        new RequestContext(
                "",
                "",
                mock(DeviceInfo.class),
                mock(AppLoginStorage.class),
                null,
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class),
                mock(UUIDProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_MeIdSignatureStorage_ShouldNotBeNull() {
        new RequestContext(
                "",
                "",
                mock(DeviceInfo.class),
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                null,
                mock(TimestampProvider.class),
                mock(UUIDProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_TimestampProvider_ShouldNotBeNull() {
        new RequestContext(
                "",
                "",
                mock(DeviceInfo.class),
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                null,
                mock(UUIDProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_IdProvider_ShouldNotBeNull() {
        new RequestContext(
                "",
                "",
                mock(DeviceInfo.class),
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class),
                null);
    }
}