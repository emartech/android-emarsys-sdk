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

        currentApplicationCode.text = resources.getString(R.string.current_application_code, Emarsys.config.applicationCode)
        currentMerchantId.text = resources.getString(R.string.current_merchant_id, Emarsys.config.merchantId)

        buttonChangeApplicationCode.setOnClickListener {
            Emarsys.config.changeApplicationCode(newApplicationCode.text.toString()) {
                if (it == null) {
                    currentApplicationCode.text = resources.getString(R.string.current_application_code, Emarsys.config.applicationCode)
                    newApplicationCode.text?.clear()
                    view.showSnackBar("ApplicationCode has been changed!")
                } else {
                    view.showSnackBar(it.message
                            ?: "Error during ApplicationCode change, Mobile Engage is disabled!")
                    currentApplicationCode.text = resources.getString(R.string.current_application_code, Emarsys.config.applicationCode)
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
}
