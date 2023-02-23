package com.emarsys.mobileengage.iam.dialog

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.emarsys.core.Mockable
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.util.log.Logger.Companion.error
import com.emarsys.core.util.log.Logger.Companion.metric
import com.emarsys.core.util.log.entry.AppEventLog
import com.emarsys.core.util.log.entry.InAppLoadingTime
import com.emarsys.core.util.log.entry.InAppLog
import com.emarsys.core.util.log.entry.OnScreenTime
import com.emarsys.mobileengage.R
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction
import com.emarsys.mobileengage.iam.model.InAppMetaData
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener

@Mockable
class IamDialog(
    private val timestampProvider: TimestampProvider,
    webViewProvider: IamWebViewProvider
): DialogFragment() {
    constructor() : this(mobileEngage().timestampProvider, mobileEngage().webViewProvider)

    companion object {
        const val TAG = "MOBILE_ENGAGE_IAM_DIALOG_TAG"
        const val CAMPAIGN_ID = "id"
        const val SID = "sid"
        const val URL = "url"
        const val REQUEST_ID = "request_id"
        const val IS_SHOWN = "isShown"
        const val ON_SCREEN_TIME = "on_screen_time"
        const val END_SCREEN_TIME = "end_screen_time"
        const val LOADING_TIME = "loading_time"
    }

    private var actions: List<OnDialogShownAction>? = null
    private lateinit var webViewContainer: FrameLayout
    private var startTime: Long = 0
    private var dismissed = false

    private val iamWebView = webViewProvider.provide()

    fun loadInApp(
        html: String,
        inAppMetaData: InAppMetaData,
        messageLoadedListener: MessageLoadedListener
    ) {
        this.iamWebView.load(html, inAppMetaData, messageLoadedListener)
    }

    fun setActions(actions: List<OnDialogShownAction>?) {
        this.actions = actions
    }

    fun setInAppLoadingTime(inAppLoadingTime: InAppLoadingTime?) {
        arguments?.putSerializable(LOADING_TIME, inAppLoadingTime)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.mobile_engage_in_app_message, container, false)
        webViewContainer = v.findViewById(R.id.mobileEngageInAppMessageContainer)
        return v
    }

    override fun onStart() {
        super.onStart()
        webViewContainer.removeAllViews()
        if (iamWebView.webView.parent == null) {
            webViewContainer.addView(iamWebView.webView)
        }

        val window = dialog?.window

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val windowParams = window?.attributes
        windowParams?.dimAmount = 0.0f
        window?.attributes = windowParams
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    override fun onResume() {
        super.onResume()
        startTime = timestampProvider.provideTimestamp()
        val args = arguments
        if (args != null) {
            val notShown = !args.getBoolean(IS_SHOWN, false)
            if (notShown) {
                actions?.forEach { action ->
                    val campaignId = args.getString(CAMPAIGN_ID)
                    val sid = args.getString(SID)
                    val url = args.getString(URL)
                    action.execute(campaignId, sid, url)
                    args.putBoolean(IS_SHOWN, true)
                }
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        saveOnScreenTime()
        retainInstance = false
        super.onCancel(dialog)
    }

    override fun dismiss() {
        saveOnScreenTime()
        retainInstance = false
        super.dismiss()
    }

    override fun onPause() {
        updateOnScreenTime()
        super.onPause()
    }

    override fun onStop() {
        webViewContainer.removeView(iamWebView.webView)
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        iamWebView.purge()
    }

    override fun onDestroyView() {
        if (dialog != null && retainInstance) {
            dialog!!.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    private fun updateOnScreenTime() {
        if (!dismissed) {
            val endScreenTime = timestampProvider.provideTimestamp()
            val currentDuration = endScreenTime - startTime
            val previousDuration = arguments?.getLong(ON_SCREEN_TIME) ?: 0
            arguments?.putLong(ON_SCREEN_TIME, previousDuration + currentDuration)
            arguments?.putLong(END_SCREEN_TIME, endScreenTime)
        }
    }

    private fun saveOnScreenTime() {
        updateOnScreenTime()
        val args = arguments
        if (args != null) {
            metric(
                InAppLog(
                    args.getSerializable(LOADING_TIME)!! as InAppLoadingTime,
                    OnScreenTime(
                        args.getLong(ON_SCREEN_TIME),
                        startTime,
                        args.getLong(END_SCREEN_TIME)
                    ),
                    args.getString(CAMPAIGN_ID)!!,
                    args.getString(REQUEST_ID)
                )
            )
        } else {
            error(
                AppEventLog(
                    "reporting iamDialog",
                    mapOf("error" to "iamDialog - arguments has been null")
                )
            )
        }
        dismissed = true
    }
}