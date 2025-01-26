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


class PreferedColorsAdapter(
    context: Context,
    @LayoutRes private val layoutResource: Int,
    private val metalColourList: List<String>,
    isFromNewItem: Boolean
) :
    ArrayAdapter<String>(context, layoutResource, metalColourList),
    Filterable {
    private var isfromnewitem = isFromNewItem
    private lateinit var fileterdcolors: List<String>
    var selectedColorList: ArrayList<String> = arrayListOf()
    var selectedchbColorList: ArrayList<Boolean> = arrayListOf()
    private var chbChecked: Boolean = false
    override fun getCount() = if (this::fileterdcolors.isInitialized) fileterdcolors.size else 0
    override fun getItem(position: Int) = fileterdcolors[position]


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return convertView ?: createView(position, parent)
    }

    private fun createView(position: Int, parent: ViewGroup?): View {

        val view =
            LayoutInflater.from(context).inflate(R.layout.row_vendors_list_additem, parent, false)
        view.VendorName.text = "${fileterdcolors[position]}"

        view.llVendorName.setOnClickListener {

            when (view.chbVendor.isChecked) {
                true -> chbChecked = true
                false -> chbChecked = false
            }
            when (chbChecked) {
                false -> {
                    chbChecked = true
                    view.chbVendor.isChecked = true
                    when (isfromnewitem) {
                        true -> (context as InventoryInfoActivity).colorselected(
                            true,
                            view.VendorName.text.toString()
                        )
                        false -> (context as InventoryInfoActivity).colorselected(
                            true,
                            view.VendorName.text.toString()
                        )
                    }

                    selectedColorList.add((view.VendorName.text.toString()))
                    selectedchbColorList.add(true)
                }
                true -> {
                    chbChecked = false
                    view.chbVendor.isChecked = false
                    when (isfromnewitem) {
                        true -> (context as InventoryInfoActivity).colordeselected(
                            false,
                            view.VendorName.text.toString()
                        )
                        false -> (context as InventoryInfoActivity).colordeselected(
                            false,
                            view.VendorName.text.toString()
                        )
                    }
                    selectedColorList.remove((view.VendorName.text.toString()))
                    selectedchbColorList.remove(true)

                }
            }

        }
        if (selectedColorList.size > 0) {
            if (selectedColorList.contains(view.VendorName.text.toString()) && selectedchbColorList.contains(
                    true
                )
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

            for (color in metalColourList) {
                if (color.toLowerCase()
                        ?.contains(input.toLowerCase())!!
                ) results.add(color)
            }

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            fileterdcolors = results?.values as ArrayList<String>
            if (results?.count!! > 0) notifyDataSetChanged()
            else notifyDataSetInvalidated()
        }
    }


    fun addChbList(
        colorNameList: List<String>?,
        colorChbList: ArrayList<Boolean>?,
        colorSelectedList: ArrayList<String>
    ) {
        this.metalColourList.apply {
            clear()
            if (colorNameList != null) {
                addAll(colorNameList)
            }
        }
        this.selectedchbColorList.apply {
            clear()
            if (colorChbList != null) {
                addAll(colorChbList)
            }
        }
        this.selectedColorList.apply {
            clear()
            if (colorSelectedList != null) {
                addAll(colorSelectedList)
            }
        }

    }

}
