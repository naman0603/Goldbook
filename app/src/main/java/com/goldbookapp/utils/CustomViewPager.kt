package com.goldbookapp.utils

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager

class CustomViewPager : ViewPager {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    internal fun getMeasureExactly(child: View, widthMeasureSpec: Int): Int {
        child.measure(
            widthMeasureSpec,
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val height = child.measuredHeight
        return View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val wrapHeight = View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.AT_MOST

        val tab = getChildAt(0) ?: return

        val width = measuredWidth
        if (wrapHeight) {
            // Keep the current measured width.
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        }
        val fragment = adapter!!.instantiateItem(this,currentItem) as Fragment
        heightMeasureSpec = getMeasureExactly(fragment.requireView(), widthMeasureSpec)



        //Log.i(Constants.TAG, "item :" + getCurrentItem() + "|height" + heightMeasureSpec);
        // super has to be called again so the new specs are treated as
        // exact measurements.
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }



}