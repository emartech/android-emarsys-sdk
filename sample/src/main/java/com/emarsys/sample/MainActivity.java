package com.emarsys.sample;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.emarsys.Emarsys;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    MainPagerAdapter adapter;
    ViewPager viewPager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new MainPagerAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    Emarsys.Inbox.resetBadgeCount(new CompletionListener() {
                        @Override
                        public void onCompleted(@Nullable Throwable errorCause) {
                            if (errorCause != null) {
                                updateBadgeCount();
                            }
                        }
                    });
                }
                updateBadgeCount();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    void updateBadgeCount() {
        Emarsys.Inbox.fetchNotifications(new ResultListener<Try<NotificationInboxStatus>>() {
            @Override
            public void onResult(@NonNull Try<NotificationInboxStatus> result) {
                NotificationInboxStatus inboxStatus = result.getResult();
                if (inboxStatus != null) {
                    adapter.setBadgeCount(inboxStatus.getBadgeCount());
                    ((NotificationInboxFragment) adapter.getItem(1)).updateList(inboxStatus.getNotifications());
                }
            }
        });
    }

}
