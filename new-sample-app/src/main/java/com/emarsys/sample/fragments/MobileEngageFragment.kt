package com.emarsys.sample.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.emarsys.Emarsys
import com.emarsys.sample.R
import com.emarsys.sample.extensions.copyToClipboard
import com.emarsys.sample.extensions.showSnackBar
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_mobile_engage.*
import org.json.JSONException
import org.json.JSONObject

class MobileEngageFragment : Fragment() {

    private companion object {
        const val TAG = "MobileEngageFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_mobile_engage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonLogin.setOnClickListener {
            val contactId = contactId.text.toString()
            Emarsys.setContact(contactId) {
                if (it != null) {
                    Log.e(TAG, it.toString())
                    view.showSnackBar("Login: failed :(")
                } else {
                    Log.i(TAG, "Login successful.")
                    loggedInText.append(contactId)
                    loggedInText.visibility = View.VISIBLE
                    view.showSnackBar("Login: OK :)")
                }
            }
        }

        buttonLogout.setOnClickListener {
            Emarsys.clearContact {
                if (it != null) {
                    Log.e(TAG, it.toString())
                } else {
                    val message = "Logout was successful."
                    Log.i(TAG, message)
                    loggedInText.visibility = View.GONE
                    view.showSnackBar(message)
                    loggedInText.text = getString(R.string.logged_in)
                }
            }
        }

        buttonTrackMessage.setOnClickListener {
            val intent = Intent()
            val id = trackMessageId.text.toString()
            val payload = Bundle()
            payload.putString("key1", "value1")
            payload.putString("u", String.format("""{"sid":"%s"}""", id))
            intent.putExtra("payload", payload)
            Emarsys.Push.trackMessageOpen(intent) {
                if (it != null) {
                    Log.e(TAG, it.toString())
                } else {
                    Log.i(TAG, "Message id: $id")
                    view.showSnackBar("Message open: OK")
                }
            }
        }

        buttonTrackCustomEvent.setOnClickListener {
            val eventName = eventName.text.toString()
            val stringAttributes = eventAttributes.text.toString()

            val attributes = mutableMapOf<String, String>()
            if (!stringAttributes.isBlank()) {
                try {
                    val jsonObject = JSONObject(stringAttributes)
                    for (key in jsonObject.keys()) {
                        attributes[key] = jsonObject.getString(key)
                    }
                    Emarsys.trackCustomEvent(eventName, attributes) {
                        if (it != null) {
                            Log.e(TAG, it.toString())
                        } else {
                            Log.i(TAG, "Custom Event Track successful")
                            view.showSnackBar("Custom Event Track successful")
                        }
                    }
                } catch (e: JSONException) {
                    view.showSnackBar("Invalid JSON format")
                    e.printStackTrace()
                }
            }
        }

        switchDoNotDisturb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Emarsys.InApp.pause()
            } else {
                Emarsys.InApp.resume()
            }
        }

        buttonGetPushToken.setOnClickListener {
            FirebaseApp.initializeApp(view.context)
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
                run {
                    val pushToken = task.result?.token

                    pushToken?.copyToClipboard(view.context)

                    view.showSnackBar("Push Token copied: $pushToken")
                }
            }
        }
    }
}
