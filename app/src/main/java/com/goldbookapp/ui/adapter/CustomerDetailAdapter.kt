package com.goldbookapp.ui.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.goldbookapp.model.CustomerDetailModel
import com.goldbookapp.ui.fragment.ContactInfoFragment
import com.goldbookapp.ui.fragment.TransactionsFragment

internal class CustomerDetailAdapter(
   var context: Context,
   fm: FragmentManager,
   var totalTabs: Int,
   var customerDetailModel: CustomerDetailModel
) :
   FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
   override fun getItem(position: Int): Fragment {
      return when (position) {
         0 -> {
            ContactInfoFragment(customerDetailModel, context)
         }
         1 -> {
            TransactionsFragment(customerDetailModel, context)
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