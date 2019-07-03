package com.emarsys.mobileengage.device;

import android.app.Activity;

import com.emarsys.core.activity.ActivityLifecycleAction;
import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.ClientServiceInternal;

public class DeviceInfoStartAction implements ActivityLifecycleAction {

    private final DeviceInfo deviceInfo;
    private final ClientServiceInternal clientInternal;
    private final Storage<Integer> deviceInfoHashStorage;

    public DeviceInfoStartAction(ClientServiceInternal clientInternal, Storage<Integer> deviceInfoHashStorage, DeviceInfo deviceInfo) {
        Assert.notNull(clientInternal, "ClientInternal must not be null!");
        Assert.notNull(deviceInfoHashStorage, "DeviceInfoHashStorage must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");

        this.clientInternal = clientInternal;
        this.deviceInfoHashStorage = deviceInfoHashStorage;
        this.deviceInfo = deviceInfo;
    }

    @Override
    public void execute(Activity activity) {
        if (deviceInfoHashStorage.get() == null || !deviceInfoHashStorage.get().equals(deviceInfo.getHash())) {
            clientInternal.trackDeviceInfo();
        }
    }
}
