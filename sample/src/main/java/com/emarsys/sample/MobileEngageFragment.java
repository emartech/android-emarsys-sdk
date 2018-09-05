package com.emarsys.sample;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.emarsys.Emarsys;
import com.emarsys.mobileengage.api.MobileEngageException;
import com.emarsys.result.CompletionListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MobileEngageFragment extends Fragment {
    private static final String TAG = "MobileEngageFragment";

    private Button appLogingAnonymous;
    private Button appLogin;
    private Button appLogout;
    private Button customEvent;
    private Button messageOpen;
    private Button pushToken;

    private EditText applicationId;
    private EditText customerId;
    private EditText eventName;
    private EditText eventAttributes;
    private EditText messageId;

    private TextView statusLabel;

    private Switch doNotDisturb;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mobile_engage, container, false);

        final CompletionListener completionListener = new CompletionListener() {
            @Override
            public void onCompleted(@Nullable Throwable cause) {
                String message;
                if (cause == null) {
                    message = "OK";
                    Log.i(TAG, message);
                } else {
                    Log.e(TAG, cause.getMessage(), cause);
                    StringBuilder sb = new StringBuilder();
                    if (cause instanceof MobileEngageException) {
                        MobileEngageException mee = (MobileEngageException) cause;
                        sb.append(mee.getStatusCode());
                        sb.append(" - ");
                    }
                    sb.append(cause.getMessage());
                    message = sb.toString();
                }
                statusLabel.append(message);
            }
        };
        statusLabel = root.findViewById(R.id.mobileEngageStatusLabel);

        appLogingAnonymous = root.findViewById(R.id.appLoginAnonymous);
        appLogin = root.findViewById(R.id.appLogin);
        appLogout = root.findViewById(R.id.appLogout);
        customEvent = root.findViewById(R.id.customEvent);
        messageOpen = root.findViewById(R.id.messageOpen);
        pushToken = root.findViewById(R.id.pushToken);

        applicationId = root.findViewById(R.id.contactFieldId);
        customerId = root.findViewById(R.id.contactFieldValue);
        eventName = root.findViewById(R.id.eventName);
        eventAttributes = root.findViewById(R.id.eventAttributes);
        messageId = root.findViewById(R.id.messageId);

        doNotDisturb = root.findViewById(R.id.doNotDisturb);

        appLogingAnonymous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Emarsys.setCustomer("");
                handleRequestSent("Anonymous login: ");
            }
        });

        appLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contactFieldId = applicationId.getText().toString();
                if (!contactFieldId.isEmpty()) {
                    String id = customerId.getText().toString();
                    Emarsys.setCustomer(id, new CompletionListener() {
                        @Override
                        public void onCompleted(@Nullable Throwable cause) {
                            completionListener.onCompleted(cause);
                            if (cause != null) {
                                ((MainActivity) getActivity()).updateBadgeCount();
                            }
                        }
                    });
                    handleRequestSent("Login: ");
                }
            }
        });

        appLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Emarsys.clearCustomer(completionListener);
                handleRequestSent("Logout: ");
            }
        });

        customEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        messageOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                String id = messageId.getText().toString();
                Bundle payload = new Bundle();
                payload.putString("key1", "value1");
                payload.putString("u", String.format("{\"sid\": \"%s\"}", id));
                intent.putExtra("payload", payload);
                Emarsys.Push.trackMessageOpen(intent, completionListener);
                handleRequestSent("Message open: ");
            }
        });

        doNotDisturb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Emarsys.InApp.pause();
                } else {
                    Emarsys.InApp.resume();
                }
            }
        });

        pushToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pushToken = FirebaseInstanceId.getInstance().getToken();
                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                String toastMessage = pushToken;
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("pushtoken", pushToken);
                    clipboard.setPrimaryClip(clip);
                    toastMessage = "Copied: " + toastMessage;
                }
                Toast.makeText(
                        v.getContext(),
                        toastMessage,
                        Toast.LENGTH_LONG).show();
            }
        });

        return root;
    }

    private void handleRequestSent(String title) {
        statusLabel.setText(title);
    }
}