package com.goldbookapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.goldbookapp.R
import kotlinx.android.synthetic.main.material_page.view.*
import kotlinx.android.synthetic.main.onboarding_activity.*

class ViewPagerAdapter(itemDetailImagesUrls: ArrayList<String>) : PagerAdapter() {
    var itemDetailImagsUrls = itemDetailImagesUrls
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val item = LayoutInflater.from(container.context).inflate(
            R.layout.material_page, container,
            false)

        Glide.with(container.context).load(itemDetailImagsUrls.get(position)).placeholder(R.drawable.ic_user_placeholder).fitCenter().into(item.itemdetail_imgview)
        container.addView(item)
        return item
    }

    override fun getCount(): Int {
        return itemDetailImagsUrls.size
    }


    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}