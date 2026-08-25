package com.emarsys.mobileengage.iam.dialog

import android.annotation.TargetApi
import android.app.Activity
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.emarsys.core.Mockable
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.core.util.log.Logger.Companion.error
import com.emarsys.core.util.log.Logger.Companion.info
import com.emarsys.core.util.log.Logger.Companion.metric
import com.emarsys.core.util.log.entry.AppEventLog
import com.emarsys.core.util.log.entry.InAppLoadingTime
import com.emarsys.core.util.log.entry.InAppLog
import com.emarsys.core.util.log.entry.OnScreenTime
import com.emarsys.mobileengage.R
import com.emarsys.mobileengage.di.mobileEngage
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction
import com.emarsys.mobileengage.iam.model.InAppMetaData
import com.emarsys.mobileengage.iam.webview.IamWebView
import com.emarsys.mobileengage.iam.webview.IamWebViewFactory
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener
import java.lang.ref.WeakReference

@Mockable
class IamDialog(
    private val timestampProvider: TimestampProvider,
    private val webViewFactory: IamWebViewFactory
) : DialogFragment() {
    constructor() : this(mobileEngage().timestampProvider, mobileEngage().webViewFactory)

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

    private var html: String? = null
    private var inAppMetaData: InAppMetaData? = null
    private var pendingPageLoadedListener: MessageLoadedListener? = null
    private var webViewLoadTriggered: Boolean = false

    private var activityReference: WeakReference<Activity>? = null

    private var iamWebView: IamWebView? = null

    fun loadInApp(
        html: String,
        inAppMetaData: InAppMetaData,
        messageLoadedListener: MessageLoadedListener,
        activity: Activity
    ) {
        this.activityReference = WeakReference(activity)
        this.html = html
        this.inAppMetaData = inAppMetaData
        this.pendingPageLoadedListener = messageLoadedListener
        if (iamWebView == null) {
            iamWebView = webViewFactory.create(activity)
        }
    }

    fun setActions(actions: List<OnDialogShownAction>?) {
        this.actions = actions
    }

    fun setInAppLoadingTime(inAppLoadingTime: InAppLoadingTime?) {
        arguments?.putSerializable(LOADING_TIME, inAppLoadingTime)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Dialog)

        val activity = activityReference?.get() ?: this.activity
        html = html ?: savedInstanceState?.getString("html")
        inAppMetaData = inAppMetaData
            ?: if (AndroidVersionUtils.isBelowTiramisu) savedInstanceState?.getSerializable("inAppMetaData") as InAppMetaData?
            else getInAppMetaDataFromBundle(savedInstanceState)
        if (iamWebView == null && activity != null) {
            iamWebView = webViewFactory.create(activity)
        }
    }

    @TargetApi(33)
    private fun getInAppMetaDataFromBundle(savedInstanceState: Bundle?) =
        savedInstanceState?.getSerializable("inAppMetaData", InAppMetaData::class.java)

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
        iamWebView?.let {
            if (it.webView.parent == null) {
                webViewContainer.addView(it.webView)
            }
        }

        val window = dialog?.window

        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        val windowParams = window?.attributes
        windowParams?.dimAmount = 0.0f
        window?.attributes = windowParams
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        triggerWebViewLoadOnce()
    }

    private fun triggerWebViewLoadOnce() {
        if (webViewLoadTriggered) return
        val htmlToLoad = html ?: return
        val metaToLoad = inAppMetaData ?: return
        webViewLoadTriggered = true
        val listener = pendingPageLoadedListener ?: MessageLoadedListener {}
        iamWebView?.load(htmlToLoad, metaToLoad, listener)
        pendingPageLoadedListener = null
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
        super.onCancel(dialog)
    }

    override fun dismiss() {
        saveOnScreenTime()
        super.dismiss()
    }

    override fun onPause() {
        updateOnScreenTime()
        super.onPause()
    }

    override fun onStop() {
        iamWebView?.let {
            webViewContainer.removeView(it.webView)
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        iamWebView?.purge()
    }

    override fun onDestroyView() {
        if (dialog != null) {
            dialog!!.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("html", html)
        outState.putSerializable("inAppMetaData", inAppMetaData)
        super.onSaveInstanceState(outState)
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
            val loadingTime = args.getSerializable(LOADING_TIME) as? InAppLoadingTime
            val campaignId = args.getString(CAMPAIGN_ID)
            if (campaignId == null) {
                error(
                    AppEventLog(
                        "reporting iamDialog",
                        mapOf("error" to "iamDialog - campaignId is null")
                    )
                )
            } else if (loadingTime == null) {
                info(
                    AppEventLog(
                        "reporting iamDialog",
                        mapOf(
                            "message" to "iamDialog - loadingTime is null",
                            "campaignId" to campaignId
                        )
                    )
                )
            } else {
                metric(
                    InAppLog(
                        loadingTime,
                        OnScreenTime(
                            args.getLong(ON_SCREEN_TIME),
                            startTime,
                            args.getLong(END_SCREEN_TIME)
                        ),
                        campaignId,
                        args.getString(REQUEST_ID)
                    )
                )
            }
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
