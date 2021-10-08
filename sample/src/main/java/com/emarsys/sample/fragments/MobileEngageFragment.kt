package com.emarsys.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.emarsys.sample.R
import com.emarsys.sample.adapters.ViewPagerAdapter
import com.emarsys.sample.databinding.FragmentMobileEngageBinding
import com.google.android.material.tabs.TabLayoutMediator

class MobileEngageFragment : Fragment() {
    companion object {
        val TAG: String = MobileEngageFragment::class.java.simpleName
    }
    private var _binding: FragmentMobileEngageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentMobileEngageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPagerAdapter = ViewPagerAdapter(requireActivity())
        viewPagerAdapter.addFragment(getString(R.string.menu_title_mobile_engage_tracking), MobileEngageFragmentTracking())
        viewPagerAdapter.addFragment(getString(R.string.menu_title_message_inbox), MessageInboxFragment())
        viewPagerAdapter.addFragment(getString(R.string.menu_title_inline_in_app), InlineInAppFragment())

        binding.viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewPagerAdapter.getTitle(position)
        }.attach()
    }
}