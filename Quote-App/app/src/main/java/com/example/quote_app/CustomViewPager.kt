package com.example.quote_app

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.viewpager.widget.ViewPager

class CustomViewPager : ViewPager {

    private var lastPositionOffsetPixels: Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
