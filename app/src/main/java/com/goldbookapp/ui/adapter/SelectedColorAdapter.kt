package com.goldbookapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.ui.activity.item.InventoryInfoActivity
import kotlinx.android.synthetic.main.row_selected_vendor_item.view.*

class SelectedColorAdapter(val context: Context, val selectedColorsList: ArrayList<String>) : RecyclerView.Adapter<SelectedColorAdapter.ViewHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedColorAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_selected_vendor_item, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: SelectedColorAdapter.ViewHolder, position: Int) {
        holder.bindItems(selectedColorsList[position],position,context)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return selectedColorsList.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(
            vendor: String,
            position: Int,
            context: Context
        ) {
            itemView.apply {
                tvrowSelectedVendor.text = " ${position+1} ${". "+vendor}"
                itemView.deleteVendor.setOnClickListener {
                    (context as InventoryInfoActivity).colordeselected(true, vendor)
                }
            }



        }
    }
}