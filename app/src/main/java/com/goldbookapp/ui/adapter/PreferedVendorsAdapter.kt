package com.goldbookapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.LayoutRes
import com.goldbookapp.R
import com.goldbookapp.ui.activity.item.InventoryInfoActivity
import kotlinx.android.synthetic.main.row_vendors_list_additem.view.*


class PreferedVendorsAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val vendorslist: List<String>,isFromNewItem:Boolean):
    ArrayAdapter<String>(context, layoutResource, vendorslist),
    Filterable {
    private var isfromnewitem = isFromNewItem
    private lateinit var fileterdvendors: List<String>
    var selectedVendorList: ArrayList<String> = arrayListOf()
    var selectedchbVendorList: ArrayList<Boolean> = arrayListOf()
    private var chbChecked:Boolean = false
    override fun getCount() = if(this::fileterdvendors.isInitialized) fileterdvendors.size else 0


    override fun getItem(position: Int) = fileterdvendors[position]


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return convertView ?: createView(position, parent)
    }

    private fun createView(position: Int, parent: ViewGroup?): View {

        val view = LayoutInflater.from(context).inflate(R.layout.row_vendors_list_additem, parent, false)
        view.VendorName.text = "${fileterdvendors[position]}"

        view.llVendorName.setOnClickListener {

            when(view.chbVendor.isChecked){
                true ->  chbChecked = true
                false ->   chbChecked = false
            }
            when(chbChecked){
                false -> {
                    chbChecked = true
                    view.chbVendor.isChecked = true
                    when(isfromnewitem){
                        true -> (context as InventoryInfoActivity).vendorselected(true, view.VendorName.text.toString())
                        false -> (context as InventoryInfoActivity).vendorselected(true, view.VendorName.text.toString())
                    }

                    selectedVendorList.add((view.VendorName.text.toString()))
                    selectedchbVendorList.add(true)
                }
                true -> {
                    chbChecked = false
                    view.chbVendor.isChecked = false
                    when(isfromnewitem){
                        true -> (context as InventoryInfoActivity).vendordeselected(false, view.VendorName.text.toString())
                        false -> (context as InventoryInfoActivity).vendordeselected(false, view.VendorName.text.toString())
                    }
                    selectedVendorList.remove((view.VendorName.text.toString()))
                    selectedchbVendorList.remove(true)

                }
            }

        }
        if (selectedVendorList.size > 0) {
                    if (selectedVendorList.contains(view.VendorName.text.toString()) && selectedchbVendorList.contains(true)
                    ) view.chbVendor.isChecked = true
                    else view.chbVendor.isChecked = false

        }


        return view
    }


    override fun getFilter() = filter
    private var filter: Filter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
            val results = FilterResults()

            val query =
                if (constraint != null && constraint.isNotEmpty()) autocomplete(constraint.toString())
                else arrayListOf()

            results.values = query
            results.count = query.size

            return results
        }

        private fun autocomplete(input: String): ArrayList<String> {
            val results = arrayListOf<String>()

            for (vendor in vendorslist) {
                if (vendor.toLowerCase()
                        ?.contains(input.toLowerCase())!!
                ) results.add(vendor)
            }

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            fileterdvendors = results?.values as ArrayList<String>
            if (results?.count!! > 0) notifyDataSetChanged()
            else notifyDataSetInvalidated()
        }

    }
    fun addChbList(
        vendorNameList: List<String>?,
        vendorChbList: ArrayList<Boolean>?,
        vendorSelectedList: ArrayList<String>
    ) {
        this.vendorslist.apply {
            clear()
            if (vendorNameList != null) {
                addAll(vendorNameList)
            }
        }
        this.selectedchbVendorList.apply {
            clear()
            if (vendorChbList != null) {
                addAll(vendorChbList)
            }
        }
        this.selectedVendorList.apply {
            clear()
            if (vendorSelectedList != null) {
                addAll(vendorSelectedList)
            }
        }

    }

}
