package com.goldbookapp.utils

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ScrollView

class CustomScrollView (context: Context, attrs: AttributeSet) : ScrollView(context, attrs) {

    private val mGestureDetector: GestureDetector?

    init {
        mGestureDetector = GestureDetector(context, YScrollDetector())
        setFadingEdgeLength(0)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (this.mGestureDetector!!.onTouchEvent(ev)) {
            true
        } else super.onInterceptTouchEvent(ev)
    }

    // Return false if we're scrolling in the x direction
    internal inner class YScrollDetector : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent, e2: MotionEvent,
            distanceX: Float, distanceY: Float
        ): Boolean {
            return Math.abs(distanceY) > Math.abs(distanceX)
        }
    }
}