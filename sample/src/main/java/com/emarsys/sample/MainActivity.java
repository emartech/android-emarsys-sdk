package com.emarsys.sample;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.inbox.InboxResultListener;
import com.emarsys.mobileengage.inbox.ResetBadgeCountResultListener;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;

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
                    MobileEngage.Inbox.resetBadgeCount(new ResetBadgeCountResultListener() {
                        @Override
                        public void onSuccess() {
                            updateBadgeCount();
                        }

                        @Override
                        public void onError(Exception cause) {

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
        MobileEngage.Inbox.fetchNotifications(new InboxResultListener<NotificationInboxStatus>() {
            @Override
            public void onSuccess(NotificationInboxStatus result) {
                adapter.setBadgeCount(result.getBadgeCount());
                ((NotificationInboxFragment) adapter.getItem(1)).updateList(result.getNotifications());
            }

            @Override
            public void onError(Exception cause) {

            }
        });
    }

}
