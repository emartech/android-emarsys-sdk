package com.emarsys.sample;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MainPagerAdapter extends FragmentPagerAdapter {
    private BaseFragment[] fragments;
    private int unreadBadgeCount;

    public MainPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        fragments = new BaseFragment[]{
                new MobileEngageFragment(),
                new NotificationInboxFragment(),
                new PredictFragment()};
    }

    @Override
    public int getCount() {
        return fragments.length;
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
        return fragments[position].getName() + additionalText;
    }

    void setBadgeCount(int unreadBadgeCount) {
        this.unreadBadgeCount = unreadBadgeCount;
        this.notifyDataSetChanged();
    }
}
