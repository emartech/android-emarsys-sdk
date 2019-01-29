package com.emarsys.mobileengage.iam;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.provider.Gettable;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider;
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction;
import com.emarsys.mobileengage.iam.dialog.action.SaveDisplayedIamAction;
import com.emarsys.mobileengage.iam.dialog.action.SendDisplayedIamAction;
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider;
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener;

import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class InAppPresenter {

    private final Gettable<Activity> currentActivityProvider;
    private Handler coreSdkHandler;
    private IamWebViewProvider webViewProvider;
    private InAppInternal inAppInternal;
    private IamDialogProvider dialogProvider;
    private Repository<ButtonClicked, SqlSpecification> buttonClickedRepository;
    private Repository<DisplayedIam, SqlSpecification> displayedIamRepository;
    private TimestampProvider timestampProvider;
    private MobileEngageInternal mobileEngageInternal;

    public InAppPresenter(
            Handler coreSdkHandler,
            IamWebViewProvider webViewProvider,
            InAppInternal inAppInternal,
            IamDialogProvider dialogProvider,
            Repository<ButtonClicked, SqlSpecification> buttonClickedRepository,
            Repository<DisplayedIam, SqlSpecification> displayedIamRepository,
            TimestampProvider timestampProvider,
            MobileEngageInternal mobileEngageInternal,
            Gettable<Activity> currentActivityProvider) {
        Assert.notNull(webViewProvider, "WebViewProvider must not be null!");
        Assert.notNull(inAppInternal, "InAppInternal must not be null!");
        Assert.notNull(dialogProvider, "DialogProvider must not be null!");
        Assert.notNull(coreSdkHandler, "CoreSdkHandler must not be null!");
        Assert.notNull(buttonClickedRepository, "ButtonClickRepository must not be null!");
        Assert.notNull(displayedIamRepository, "DisplayedIamRepository must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(mobileEngageInternal, "MobileEngageInternal must not be null!");
        Assert.notNull(currentActivityProvider, "CurrentActivityProvider must not be null!");
        this.webViewProvider = webViewProvider;
        this.inAppInternal = inAppInternal;
        this.dialogProvider = dialogProvider;
        this.coreSdkHandler = coreSdkHandler;
        this.buttonClickedRepository = buttonClickedRepository;
        this.displayedIamRepository = displayedIamRepository;
        this.timestampProvider = timestampProvider;
        this.mobileEngageInternal = mobileEngageInternal;
        this.currentActivityProvider = currentActivityProvider;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void present(String id, String requestId, String html, final MessageLoadedListener messageLoadedListener) {
        final IamDialog iamDialog = dialogProvider.provideDialog(id, requestId);
        setupDialogWithActions(iamDialog);

        IamJsBridge jsBridge = new IamJsBridge(
                inAppInternal,
                buttonClickedRepository,
                id,
                coreSdkHandler,
                mobileEngageInternal,
                currentActivityProvider);
        webViewProvider.loadMessageAsync(html, jsBridge, new MessageLoadedListener() {
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
                if (messageLoadedListener != null) {
                    messageLoadedListener.onMessageLoaded();
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setupDialogWithActions(IamDialog iamDialog) {
        OnDialogShownAction saveDisplayedIamAction = new SaveDisplayedIamAction(
                coreSdkHandler,
                displayedIamRepository,
                timestampProvider);

        OnDialogShownAction sendDisplayedIamAction = new SendDisplayedIamAction(
                coreSdkHandler,
                mobileEngageInternal);

        iamDialog.setActions(Arrays.asList(
                saveDisplayedIamAction,
                sendDisplayedIamAction));
    }
}
