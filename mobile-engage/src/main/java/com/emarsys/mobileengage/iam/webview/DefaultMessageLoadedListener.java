package com.emarsys.mobileengage.iam.webview;

import android.app.Activity;
import android.os.Build;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.provider.Gettable;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.iam.dialog.IamDialog;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class DefaultMessageLoadedListener implements MessageLoadedListener {

    private final Gettable<Activity> currentActivityProvider;
    private IamDialog iamDialog;
    private ResponseModel responseModel;
    private Repository<Map<String, Object>, SqlSpecification> logRepository;
    private TimestampProvider timestampProvider;

    public DefaultMessageLoadedListener(
            IamDialog iamDialog,
            Repository<Map<String, Object>, SqlSpecification> logRepository,
            ResponseModel responseModel,
            TimestampProvider timestampProvider,
            Gettable<Activity> currentActivityProvider) {
        Assert.notNull(iamDialog, "IamDialog must not be null!");
        Assert.notNull(logRepository, "LogRepository must not be null!");
        Assert.notNull(responseModel, "ResponseModel must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(currentActivityProvider, "CurrentActivityProvider must not be null!");
        this.iamDialog = iamDialog;
        this.logRepository = logRepository;
        this.responseModel = responseModel;
        this.timestampProvider = timestampProvider;
        this.currentActivityProvider = currentActivityProvider;
    }

    @Override
    public void onMessageLoaded() {
        Activity currentActivity = currentActivityProvider.get();
        if (currentActivity instanceof AppCompatActivity) {
            FragmentManager fragmentManager = ((AppCompatActivity) currentActivity).getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(IamDialog.TAG);
            if (fragment == null) {
                iamDialog.show(fragmentManager, IamDialog.TAG);
            }
        }
        logLoadingTime();
    }

    private void logLoadingTime() {
        Map<String, Object> metric = new HashMap<>();
        metric.put("loading_time", timestampProvider.provideTimestamp() - responseModel.getTimestamp());
        metric.put("id", responseModel.getRequestModel().getId());
        logRepository.add(metric);
    }
}


