@file:Suppress("DEPRECATION")

package com.example.clock

import android.Manifest
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.clock.fragments.AlarmFragment
import com.example.clock.fragments.HomeFragment
import com.example.clock.fragments.StopwatchFragment
import com.example.clock.fragments.TimerFragment

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var homeBtn: ImageView
    private lateinit var timerBtn: ImageView
    private lateinit var stopperBtn: ImageView
    private lateinit var alarmBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Clock)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = ViewPagerAdapter(supportFragmentManager)

        homeBtn = findViewById(R.id.btnHome)
        timerBtn = findViewById(R.id.btnTimer)
        stopperBtn = findViewById(R.id.btnStopwatch)
        alarmBtn = findViewById(R.id.btnAlarm)

        homeBtn.setOnClickListener{
            viewPager.currentItem = 0
        }
        timerBtn.setOnClickListener{
            viewPager.currentItem = 1
        }
        stopperBtn.setOnClickListener{
            viewPager.currentItem = 2
        }
        alarmBtn.setOnClickListener{
            viewPager.currentItem = 3
        }

        requestPermissions()

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> supportActionBar?.title = "Clock"
                    1 -> supportActionBar?.title = "Timer"
                    2 -> supportActionBar?.title = "Stop Clock"
                    3 -> supportActionBar?.title = "Alarm"
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

    }

    private inner class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount(): Int {
            return NUM_PAGES
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment()
                1 -> TimerFragment()
                2 -> StopwatchFragment()
                3 -> AlarmFragment()
                else -> throw IllegalArgumentException("Invalid page position: $position")
            }
        }
    }

    companion object {
        private const val NUM_PAGES = 4
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }
}
