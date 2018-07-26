package com.emarsys.mobileengage.iam.dialog;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.support.test.filters.SdkSuppress;

import java.util.concurrent.CountDownLatch;

import static android.os.Build.VERSION_CODES.KITKAT;

@SuppressLint("ValidFragment")
@SdkSuppress(minSdkVersion = KITKAT)
public class TestIamDialog extends IamDialog {

    CountDownLatch resumeLatch;
    CountDownLatch pauseLatch;
    CountDownLatch stopLatch;
    CountDownLatch cancelLatch;

    public static TestIamDialog create(
            String campaignId,
            CountDownLatch resumeLatch,
            CountDownLatch pauseLatch,
            CountDownLatch stopLatch,
            CountDownLatch cancelLatch) {
        IamDialog iamDialog = IamDialog.create(campaignId);

        TestIamDialog testIamDialog = new TestIamDialog(resumeLatch, pauseLatch, stopLatch, cancelLatch);
        testIamDialog.setArguments(iamDialog.getArguments());

        return testIamDialog;
    }

    public TestIamDialog(
            CountDownLatch resumeLatch,
            CountDownLatch pauseLatch,
            CountDownLatch stopLatch,
            CountDownLatch cancelLatch) {
        this.resumeLatch = resumeLatch;
        this.pauseLatch = pauseLatch;
        this.stopLatch = stopLatch;
        this.cancelLatch = cancelLatch;
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeLatch.countDown();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        cancelLatch.countDown();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseLatch.countDown();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLatch.countDown();
    }
}