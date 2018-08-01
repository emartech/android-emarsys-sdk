package com.emarsys.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class MainPagerAdapter extends FragmentPagerAdapter {
    private static int ITEMS = 2;
    private static String[] tabNames = new String[]{"Mobile Engage", "Notification Inbox"};
    private Fragment[] fragments;
    private int unreadBadgeCount;

    public MainPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        fragments = new Fragment[]{new MobileEngageFragment(), new NotificationInboxFragment()};
    }

    @Override
    public int getCount() {
        return ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
        if (position < fragments.length) {
            return fragments[position];
        } else {
            return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String additionalText = "";
        if (position == 1 && unreadBadgeCount > 0) {
            additionalText = " (" + unreadBadgeCount + ")";
        }
        return tabNames[position] + additionalText;
    }

    void setBadgeCount(int unreadBadgeCount) {
        this.unreadBadgeCount = unreadBadgeCount;
        this.notifyDataSetChanged();
    }
}
