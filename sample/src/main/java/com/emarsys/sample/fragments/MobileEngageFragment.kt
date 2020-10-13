package com.emarsys.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.emarsys.sample.R
import com.emarsys.sample.adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_mobile_engage.*

class MobileEngageFragment : Fragment() {

    companion object {
        val TAG: String = MobileEngageFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_mobile_engage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPagerAdapter = ViewPagerAdapter(requireActivity())
        viewPagerAdapter.addFragment(getString(R.string.menu_title_mobile_engage_tracking), MobileEngageFragmentTracking())
        viewPagerAdapter.addFragment(getString(R.string.menu_title_inbox), InboxFragment())
        viewPagerAdapter.addFragment(getString(R.string.menu_title_message_inbox), MessageInboxFragment())
        viewPagerAdapter.addFragment(getString(R.string.menu_title_inline_in_app), InlineInAppFragment())

        viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = viewPagerAdapter.getTitle(position)
        }.attach()
    }
}