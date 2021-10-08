package com.emarsys.sample.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.emarsys.Emarsys
import com.emarsys.sample.MainActivity
import com.emarsys.sample.databinding.FragmentMobileEngageTrackingBinding
import com.emarsys.sample.extensions.copyToClipboard
import com.emarsys.sample.extensions.showSnackBar
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.huawei.hms.aaid.HmsInstanceId
import org.json.JSONException
import org.json.JSONObject

class MobileEngageFragmentTracking : Fragment() {
    companion object {
        val TAG: String = MobileEngageFragmentTracking::class.java.simpleName
        const val REQUEST_LOCATION_PERMISSIONS = 99
        const val HUAWEI_APP_ID = "104446913"
        const val HUAWEI_PUSH_SCOPE = "com.emarsys.sample"
    }
    private var _binding : FragmentMobileEngageTrackingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMobileEngageTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonTrackCustomEvent.setOnClickListener {
            val eventName = binding.eventName.text.toString()
            val stringAttributes = binding.eventAttributes.text.toString()

            val attributes = mutableMapOf<String, String>()
            if (!stringAttributes.isBlank()) {
                try {
                    val jsonObject = JSONObject(stringAttributes)
                    for (key in jsonObject.keys()) {
                        attributes[key] = jsonObject.getString(key)
                    }
                } catch (e: JSONException) {
                    view.showSnackBar("Invalid JSON format")
                    e.printStackTrace()
                }
            }
            Emarsys.trackCustomEvent(eventName, attributes) {
                if (it != null) {
                    Log.e(TAG, it.toString())
                } else {
                    Log.i(TAG, "Custom Event Track successful")
                    view.showSnackBar("Custom Event Track successful")
                }
            }
        }

        binding.switchDoNotDisturb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Emarsys.inApp.pause()
            } else {
                Emarsys.inApp.resume()
            }
        }

        binding.buttonTrackPushToken.setOnClickListener {
            if (GoogleApiAvailabilityLight.getInstance()
                    .isGooglePlayServicesAvailable(activity) == ConnectionResult.SUCCESS
            ) {
                FirebaseApp.initializeApp(view.context)
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    task.addOnSuccessListener {
                        val pushToken = it

                        Emarsys.push.pushToken = pushToken
                        view.showSnackBar("Push Token tracked")
                    }
                    task.addOnFailureListener {
                        it.printStackTrace()
                        view.showSnackBar("Something went wrong!")
                    }
                }
            } else {
                Thread {
                    val pushToken = HmsInstanceId.getInstance(activity)
                        .getToken(HUAWEI_APP_ID, HUAWEI_PUSH_SCOPE)

                    activity?.runOnUiThread {
                        Emarsys.push.pushToken = pushToken
                    }
                }.start()
            }
        }

        binding.buttonCopyPushToken.setOnClickListener {
            if (GoogleApiAvailabilityLight.getInstance()
                    .isGooglePlayServicesAvailable(activity) == ConnectionResult.SUCCESS
            ) {
                FirebaseApp.initializeApp(view.context)
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    run {
                        task.addOnSuccessListener {
                            val pushToken = it
                            pushToken?.copyToClipboard(view.context)
                            view.showSnackBar("Push Token copied: $pushToken")
                        }
                        task.addOnFailureListener {
                            it.printStackTrace()
                            view.showSnackBar("Something went wrong!")
                        }
                    }
                }
            } else {
                Thread {
                    val pushToken = HmsInstanceId.getInstance(activity)
                        .getToken(HUAWEI_APP_ID, HUAWEI_PUSH_SCOPE)
                    activity?.runOnUiThread {
                        pushToken?.copyToClipboard(view.context)
                        view.showSnackBar("Push Token copied: $pushToken")
                    }
                }.start()
            }
        }

        binding.buttonEnableGeofence.setOnClickListener {
            if ((activity as MainActivity).checkLocationPermission()) {
                Emarsys.geofence.enable {
                    view.showSnackBar("Geofence has been Enabled!")
                }
            }

        }

        binding.buttonDisableGeofence.setOnClickListener {
            Emarsys.geofence.disable()
            view.showSnackBar("Geofence has been Disabled!")
        }
    }
}
