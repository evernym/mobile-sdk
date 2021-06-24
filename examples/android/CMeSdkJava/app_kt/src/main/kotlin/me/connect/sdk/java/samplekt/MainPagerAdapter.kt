package me.connect.sdk.java.samplekt

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.connect.sdk.java.samplekt.history.HistoryFragment
import me.connect.sdk.java.samplekt.homepage.HomePageFragment


class MainPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment = when (position) {
        0 -> HomePageFragment.newInstance()
        1 -> HistoryFragment.newInstance()
        else -> HomePageFragment.newInstance()
    }


    override fun getPageTitle(position: Int): CharSequence? = when (position) {
        0 -> "Home"
        1 -> "History"
        else -> null
    }

    override fun getCount(): Int = 2
}
