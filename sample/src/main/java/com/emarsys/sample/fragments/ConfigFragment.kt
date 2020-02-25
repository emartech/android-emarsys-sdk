package com.emarsys.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.emarsys.Emarsys
import com.emarsys.sample.R
import com.emarsys.sample.extensions.showSnackBar
import kotlinx.android.synthetic.main.fragment_config.*

class ConfigFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentApplicationCode.text = resources.getString(R.string.current_application_code, Emarsys.config.applicationCode, Emarsys.config.contactFieldId)
        currentMerchantId.text = resources.getString(R.string.current_merchant_id, Emarsys.config.merchantId)

        buttonChangeApplicationCode.setOnClickListener {
            if (newContactFieldId.text.isNullOrEmpty()) {
                Emarsys.config.changeApplicationCode(newApplicationCode.text.toString()) { throwable ->
                    onAppCodeChange(throwable, view)
                }
            } else {
                Emarsys.config.changeApplicationCode(newApplicationCode.text.toString(), newContactFieldId.text.toString().toInt()) { throwable ->
                    onAppCodeChange(throwable, view)
                }
            }
        }

        buttonChangeMerchantId.setOnClickListener {
            Emarsys.config.changeMerchantId(newMerchantId.text.toString())
            currentMerchantId.text = resources.getString(R.string.current_merchant_id, Emarsys.config.merchantId)
            newMerchantId.text?.clear()
            view.showSnackBar("MerchantId has been changed!")
        }

    }

    private fun onAppCodeChange(throwable: Throwable?, view: View) {
        if (throwable == null) {
            currentApplicationCode.text = resources.getString(R.string.current_application_code, Emarsys.config.applicationCode, Emarsys.config.contactFieldId)
            newApplicationCode.text?.clear()
            newContactFieldId.text?.clear()
            view.showSnackBar("ApplicationCode has been changed!")
        } else {
            view.showSnackBar(throwable.message
                    ?: "Error during ApplicationCode change, Mobile Engage is disabled!")
            currentApplicationCode.text = resources.getString(R.string.current_application_code, Emarsys.config.applicationCode, Emarsys.config.contactFieldId)
        }
    }
}
