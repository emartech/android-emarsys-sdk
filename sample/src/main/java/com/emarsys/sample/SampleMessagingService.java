package com.emarsys.sample;

import android.content.SharedPreferences;

import com.emarsys.service.EmarsysMessagingService;

public class SampleMessagingService extends EmarsysMessagingService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("sample", MODE_PRIVATE);
        sharedPreferences.edit().putString("push_token", token).apply();
    }
}
