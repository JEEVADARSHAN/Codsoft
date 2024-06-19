package com.example.clock

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.clock.fragments.AlarmFragment
import com.example.clock.fragments.HomeFragment
import com.example.clock.fragments.StopwatchFragment
import com.example.clock.fragments.TimerFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val NUM_PAGES = 3

    override fun getItemCount(): Int {
        return NUM_PAGES
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> StopwatchFragment()
            2 -> TimerFragment()
            3 -> AlarmFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
