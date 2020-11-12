package com.emarsys.sample.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.inapp.ui.InlineInAppView
import com.emarsys.sample.R
import com.emarsys.sample.extensions.showSnackBar
import kotlinx.android.synthetic.main.fragment_inline_in_app.*

class InlineInAppFragment : Fragment() {
    companion object {
        val TAG: String = InlineInAppFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inline_in_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inlineInAppFullyFromXml.onAppEventListener = { property, json ->
            view.showSnackBar("AppEvent - $property, $json")
        }
        inlineInAppFullyFromXml.onCloseListener = {
            inlineInAppFullyFromXml.visibility = View.GONE
        }

        inlineInAppFromXmlAndCode.loadInApp("iace")
        inlineInAppFromXmlAndCode.onCompletionListener = CompletionListener {
            Log.d(TAG, "Inline in-App has been showed")
        }
        inlineInAppFromXmlAndCode.onCloseListener = {
            inlineInAppFromXmlAndCode.visibility = View.GONE
        }

        inlineInAppFromXmlAndCode.onAppEventListener = { property, json ->
            view.showSnackBar("AppEvent - $property, $json")
        }

        showInlineInApp.setOnClickListener {
            val inlineInApp = InlineInAppView(it.context)
            inlineInApp.onCloseListener = {
                inlineInApp.visibility = View.GONE
            }
            inlineInAppContainer.addView(inlineInApp)
            inlineInAppContainer.invalidate()

            inlineInApp.loadInApp(viewId.text.toString())
        }
    }
}