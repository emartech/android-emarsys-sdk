package com.emarsys.sample.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.inapp.ui.InlineInAppView
import com.emarsys.sample.databinding.FragmentInlineInAppBinding
import com.emarsys.sample.extensions.showSnackBar

class InlineInAppFragment : Fragment() {
    companion object {
        val TAG: String = InlineInAppFragment::class.java.simpleName
    }
    private var _binding: FragmentInlineInAppBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInlineInAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.inlineInAppFullyFromXml.onAppEventListener = { property, json ->
            view.showSnackBar("AppEvent - $property, $json")
        }
        binding.inlineInAppFullyFromXml.onCloseListener = {
            binding.inlineInAppFullyFromXml.visibility = View.GONE
        }

        binding.inlineInAppFromXmlAndCode.loadInApp("iace")
        binding.inlineInAppFromXmlAndCode.onCompletionListener = CompletionListener {
            Log.d(TAG, "Inline in-App has been showed")
        }
        binding.inlineInAppFromXmlAndCode.onCloseListener = {
            binding.inlineInAppFromXmlAndCode.visibility = View.GONE
        }

        binding.inlineInAppFromXmlAndCode.onAppEventListener = { property, json ->
            view.showSnackBar("AppEvent - $property, $json")
        }

        binding.showInlineInApp.setOnClickListener {
            val inlineInApp = InlineInAppView(it.context)
            inlineInApp.onCloseListener = {
                inlineInApp.visibility = View.GONE
            }
            inlineInApp.onCompletionListener = CompletionListener { throwable ->
                Log.e("InlineInApp", "error", throwable)
            }
            binding.inlineInAppContainer.addView(inlineInApp)
            binding.inlineInAppContainer.invalidate()

            inlineInApp.loadInApp(binding.viewId.text.toString())
        }
    }
}