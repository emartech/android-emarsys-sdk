package com.emarsys.mobileengage.iam.dialog;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.OnScreenTime;
import com.emarsys.mobileengage.R;
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction;
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class IamDialog extends DialogFragment {

    public static final String TAG = "MOBILE_ENGAGE_IAM_DIALOG_TAG";
    public static final String CAMPAIGN_ID = "id";
    public static final String SID = "sid";
    public static final String URL = "url";
    public static final String REQUEST_ID = "request_id";
    public static final String IS_SHOWN = "isShown";
    public static final String ON_SCREEN_TIME = "on_screen_time";
    public static final String END_SCREEN_TIME = "end_screen_time";

    private List<OnDialogShownAction> actions;
    private FrameLayout webViewContainer;
    private WebView webView;
    private long startTime;
    private boolean dismissed;

    TimestampProvider timestampProvider;

    public static IamDialog create(String campaignId, String sid, String url, String requestId) {
        Assert.notNull(campaignId, "CampaignId must not be null!");

        IamDialog iamDialog = new IamDialog();
        Bundle bundle = new Bundle();
        bundle.putString(CAMPAIGN_ID, campaignId);
        bundle.putString(SID, sid);
        bundle.putString(URL, url);
        bundle.putString(REQUEST_ID, requestId);
        iamDialog.setArguments(bundle);
        return iamDialog;
    }

    public IamDialog() {
        timestampProvider = new TimestampProvider();
    }

    public void setActions(List<OnDialogShownAction> actions) {
        Assert.elementsNotNull(actions, "Actions must not be null!");
        this.actions = actions;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.mobile_engage_in_app_message, container, false);

        webView = new IamWebViewProvider(getActivity().getApplicationContext()).provideWebView();
        webViewContainer = v.findViewById(R.id.mobileEngageInAppMessageContainer);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

         webViewContainer.addView(webView);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.0f;
        window.setAttributes(windowParams);

        getDialog().getWindow()
                .setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onResume() {
        super.onResume();
        startTime = timestampProvider.provideTimestamp();

        Bundle args = getArguments();
        boolean notShown = !args.getBoolean(IS_SHOWN, false);

        if (actions != null && notShown) {
            for (OnDialogShownAction action : actions) {
                String campaignId = args.getString(CAMPAIGN_ID);
                String sid = args.getString(SID);
                String url = args.getString(URL);
                action.execute(campaignId, sid, url);
                args.putBoolean(IS_SHOWN, true);
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        saveOnScreenTime();
        setRetainInstance(false);
        super.onCancel(dialog);
    }

    @Override
    public void dismiss() {
        saveOnScreenTime();
        setRetainInstance(false);
        super.dismiss();
    }

    @Override
    public void onPause() {
        updateOnScreenTime();
        super.onPause();
    }

    @Override
    public void onStop() {
        webViewContainer.removeView(webView);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webView.removeAllViews();
        webView.destroy();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    private void updateOnScreenTime() {
        if (!dismissed) {
            long endScreenTime = timestampProvider.provideTimestamp();
            long currentDuration = endScreenTime - startTime;
            long previousDuration = getArguments().getLong(ON_SCREEN_TIME);
            getArguments().putLong(ON_SCREEN_TIME, previousDuration + currentDuration);
            getArguments().putLong(END_SCREEN_TIME, endScreenTime);
        }
    }

    private void saveOnScreenTime() {
        updateOnScreenTime();
        Bundle args = getArguments();
        Logger.log(new OnScreenTime(
                args.getLong(ON_SCREEN_TIME),
                startTime,
                args.getLong(END_SCREEN_TIME),
                args.getString(CAMPAIGN_ID),
                args.getString(REQUEST_ID)));
        dismissed = true;
    }
}
