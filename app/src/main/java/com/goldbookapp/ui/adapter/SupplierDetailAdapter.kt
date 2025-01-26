package com.goldbookapp.ui.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.goldbookapp.ui.fragment.SupplierContactInfoFrgment
import com.goldbookapp.ui.fragment.SupplierTransactionsFragment


@Suppress("DEPRECATION")
internal class SupplierDetailAdapter(
    var context: Context,
    fm: FragmentManager,
    var totalTabs: Int,
    var supplierDetailModel: SupplierDetailModel
) :
    FragmentStatePagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                SupplierContactInfoFrgment(supplierDetailModel, context)
            }
            1 -> {
                SupplierTransactionsFragment(supplierDetailModel, context)
            }
            else -> getItem(position)
        }
    }
    override fun getCount(): Int {
        return totalTabs
    }

    override fun getItemPosition(`object`: Any): Int {
        return super.getItemPosition(`object`)
    }
}