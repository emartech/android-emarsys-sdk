package com.emarsys.mobileengage.iam.dialog;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.database.repository.log.LogRepository;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.R;
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction;
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class IamDialog extends DialogFragment {

    public static final String TAG = "MOBILE_ENGAGE_IAM_DIALOG_TAG";
    public static final String CAMPAIGN_ID = "id";
    public static final String IS_SHOWN = "isShown";
    public static final String ON_SCREEN_TIME = "on_screen_time";

    private List<OnDialogShownAction> actions;
    private FrameLayout webViewContainer;
    private WebView webView;
    private long startTime;
    private boolean dismissed;

    TimestampProvider timestampProvider;
    Repository<Map<String, Object>, SqlSpecification> logRepository;

    public static IamDialog create(String campaignId) {
        Assert.notNull(campaignId, "CampaignId must not be null!");
        IamDialog iamDialog = new IamDialog();
        Bundle bundle = new Bundle();
        bundle.putString(CAMPAIGN_ID, campaignId);
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
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Dialog);
        logRepository = new LogRepository(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.mobile_engage_in_app_message, container, false);

        webView = new IamWebViewProvider().provideWebView();
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
                action.execute(campaignId);
                args.putBoolean(IS_SHOWN, true);
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        saveOnScreenTime();
        super.onCancel(dialog);
    }

    @Override
    public void dismiss() {
        saveOnScreenTime();
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

    private void updateOnScreenTime() {
        if (!dismissed) {
            long currentDuration = timestampProvider.provideTimestamp() - startTime;
            long previousDuration = getArguments().getLong(ON_SCREEN_TIME);
            getArguments().putLong(ON_SCREEN_TIME, previousDuration + currentDuration);
        }
    }

    private void saveOnScreenTime() {
        updateOnScreenTime();
        Map<String, Object> onScreenMetric = new HashMap<>();
        Bundle args = getArguments();
        onScreenMetric.put(CAMPAIGN_ID, args.getString(CAMPAIGN_ID));
        onScreenMetric.put(ON_SCREEN_TIME, args.getLong(ON_SCREEN_TIME));
        logRepository.add(onScreenMetric);
        dismissed = true;
    }
}
