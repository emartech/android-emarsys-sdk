package com.emarsys.sample.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val pages: MutableList<Pair<String, Fragment>> = mutableListOf()

    fun addFragment(title: String, fragment: Fragment) {
        pages.add(title to fragment)
    }

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment = pages[position].second

    fun getTitle(position: Int): String = pages[position].first
}