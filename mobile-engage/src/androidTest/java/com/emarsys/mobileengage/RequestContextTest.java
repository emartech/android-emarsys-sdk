package com.emarsys.mobileengage;

import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.storage.Storage;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
public class RequestContextTest {

    private int contactFieldId = 2;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationCode_mustNotBeNull() {
        new RequestContext(
                null,
                "",
                contactFieldId,
                mock(DeviceInfo.class),
                mock(TimestampProvider.class),
                mock(UUIDProvider.class),
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationPassword_mustNotBeNull() {
        new RequestContext(
                "",
                null,
                contactFieldId,
                mock(DeviceInfo.class),
                mock(TimestampProvider.class),
                mock(UUIDProvider.class),
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_DeviceInfo_mustNotBeNull() {
        new RequestContext(
                "",
                "",
                contactFieldId,
                null,
                mock(TimestampProvider.class),
                mock(UUIDProvider.class),
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_TimestampProvider_mustNotBeNull() {
        new RequestContext(
                "",
                "",
                contactFieldId,
                mock(DeviceInfo.class),
                null,
                mock(UUIDProvider.class),
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_IdProvider_mustNotBeNull() {
        new RequestContext(
                "",
                "",
                contactFieldId,
                mock(DeviceInfo.class),
                mock(TimestampProvider.class),
                null,
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_clientStateStorage_mustNotBeNull() {
        new RequestContext(
                "",
                "",
                contactFieldId,
                mock(DeviceInfo.class),
                mock(TimestampProvider.class),
                mock(UUIDProvider.class),
                null,
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_contactTokenStorage_mustNotBeNull() {
        new RequestContext(
                "",
                "",
                contactFieldId,
                mock(DeviceInfo.class),
                mock(TimestampProvider.class),
                mock(UUIDProvider.class),
                mock(Storage.class),
                null,
                mock(Storage.class),
                mock(Storage.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_refreshTokenStorage_mustNotBeNull() {
        new RequestContext(
                "",
                "",
                contactFieldId,
                mock(DeviceInfo.class),
                mock(TimestampProvider.class),
                mock(UUIDProvider.class),
                mock(Storage.class),
                mock(Storage.class),
                null,
                mock(Storage.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_contactFieldValueStorage_mustNotBeNull() {
        new RequestContext(
                "",
                "",
                contactFieldId,
                mock(DeviceInfo.class),
                mock(TimestampProvider.class),
                mock(UUIDProvider.class),
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class),
                null
        );
    }
}