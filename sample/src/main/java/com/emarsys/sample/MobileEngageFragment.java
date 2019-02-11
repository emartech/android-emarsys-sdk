package com.emarsys.sample;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.emarsys.Emarsys;
import com.emarsys.core.api.ResponseErrorException;
import com.emarsys.core.api.result.CompletionListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MobileEngageFragment extends BaseFragment {
    private static final String TAG = "MobileEngageFragment";

    private EditText contactFieldValue;
    private EditText eventName;
    private EditText eventAttributes;
    private EditText messageId;

    private TextView statusLabel;

    @Override
    public String getName() {
        return "Mobile Engage";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mobile_engage, container, false);

        final CompletionListener completionListener = errorCause -> {
            String message;
            if (errorCause == null) {
                message = "OK";
                Log.i(TAG, message);
            } else {
                Log.e(TAG, errorCause.getMessage(), errorCause);
                StringBuilder sb = new StringBuilder();
                if (errorCause instanceof ResponseErrorException) {
                    ResponseErrorException mee = (ResponseErrorException) errorCause;
                    sb.append(mee.getStatusCode());
                    sb.append(" - ");
                }
                sb.append(errorCause.getMessage());
                message = sb.toString();
            }
            statusLabel.append(message);
        };
        statusLabel = root.findViewById(R.id.mobileEngageStatusLabel);

        contactFieldValue = root.findViewById(R.id.contactFieldValue);
        eventName = root.findViewById(R.id.eventName);
        eventAttributes = root.findViewById(R.id.eventAttributes);
        messageId = root.findViewById(R.id.messageId);

        root.findViewById(R.id.appLoginAnonymous).setOnClickListener(v -> {
            Emarsys.setAnonymousContact(completionListener);
            handleRequestSent("Anonymous login: ");
        });

        root.findViewById(R.id.appLogin).setOnClickListener(v -> {
            String id = contactFieldValue.getText().toString();
            Emarsys.setContact(id, errorCause -> {
                completionListener.onCompleted(errorCause);
                if (errorCause != null) {
                    ((MainActivity) getActivity()).updateBadgeCount();
                }
            });
            handleRequestSent("Login: ");
        });

        root.findViewById(R.id.appLogout).setOnClickListener(v -> {
            Emarsys.clearContact(completionListener);
            handleRequestSent("Logout: ");
        });

        root.findViewById(R.id.customEvent).setOnClickListener(v -> {
            String name = eventName.getText().toString();
            String attributesString = eventAttributes.getText().toString();

            Map<String, String> attributes = null;
            if (!attributesString.isEmpty()) {
                try {
                    attributes = new HashMap<>();
                    JSONObject json = new JSONObject(attributesString);
                    Iterator<String> keys = json.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        attributes.put(key, json.getString(key));
                    }
                } catch (JSONException e) {
                    Log.w(TAG, "Event attributes edittext content is not a valid JSON!");
                }
            }

            Emarsys.trackCustomEvent(name, attributes, completionListener);
            handleRequestSent("Custom event: ");
        });

        root.findViewById(R.id.messageOpen).setOnClickListener(v -> {
            Intent intent = new Intent();
            String id = messageId.getText().toString();
            Bundle payload = new Bundle();
            payload.putString("key1", "value1");
            payload.putString("u", String.format("{\"sid\": \"%s\"}", id));
            intent.putExtra("payload", payload);
            Emarsys.Push.trackMessageOpen(intent, completionListener);
            handleRequestSent("Message open: ");
        });

        root.<Switch>findViewById(R.id.doNotDisturb).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Emarsys.InApp.pause();
            } else {
                Emarsys.InApp.resume();
            }
        });

        root.findViewById(R.id.pushToken).setOnClickListener(view -> FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(
                getActivity(),
                task -> {
                    ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    String pushToken = task.getResult().getToken();
                    String toastMessage = pushToken;
                    if (clipboard != null) {
                        ClipData clip = ClipData.newPlainText("pushtoken", pushToken);
                        clipboard.setPrimaryClip(clip);
                        toastMessage = "Copied: " + toastMessage;
                    }
                    Toast.makeText(
                            view.getContext(),
                            toastMessage,
                            Toast.LENGTH_LONG).show();
                }
        ));

        return root;
    }

    private void handleRequestSent(String title) {
        statusLabel.setText(title);
    }
}