package com.emarsys.mobileengage.device;

import android.app.Activity;

import com.emarsys.core.activity.ActivityLifecycleAction;
import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.client.ClientServiceInternal;

public class DeviceInfoStartAction implements ActivityLifecycleAction {

    private final DeviceInfo deviceInfo;
    private final ClientServiceInternal clientInternal;
    private final Storage<String> deviceInfoPayloadStorage;

    public DeviceInfoStartAction(ClientServiceInternal clientInternal, Storage<String> deviceInfoPayloadStorage, DeviceInfo deviceInfo) {
        Assert.notNull(clientInternal, "ClientInternal must not be null!");
        Assert.notNull(deviceInfoPayloadStorage, "DeviceInfoPayloadStorage must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");

        this.clientInternal = clientInternal;
        this.deviceInfoPayloadStorage = deviceInfoPayloadStorage;
        this.deviceInfo = deviceInfo;
    }

    @Override
    public void execute(Activity activity) {
        if (deviceInfoPayloadStorage.get() == null || !deviceInfoPayloadStorage.get().equals(deviceInfo.getDeviceInfoPayload())) {
            clientInternal.trackDeviceInfo();
        }
    }
}
