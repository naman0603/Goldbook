package com.goldbookapp.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.MetalColourModel
import com.goldbookapp.ui.activity.settings.MetalColoursDetailsActivity
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_all_item.view.*

class MetalColourListAdapter(private val metalColourList: ArrayList<MetalColourModel.DataMetalColour>) : RecyclerView.Adapter<MetalColourListAdapter.DataViewHolder>(){

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(metalColourModel: MetalColourModel.DataMetalColour) {
            itemView.apply {
                tvNameRowAllItem.text = metalColourModel.colour_name + " (Code: "+ metalColourModel.colour_code + ")"
                tvStockRowAllItem.visibility = View.VISIBLE
                lngrosswt.visibility = View.GONE
                if(metalColourModel.status?.toString().equals("1",true)!!){
                    tvStockRowAllItem.text = context.getString(R.string.active)
                    tvStockRowAllItem.setTextColor(resources.getColor(R.color.credit_color))
                }
                else{
                    tvStockRowAllItem.text = context.getString(R.string.inactive)
                    tvStockRowAllItem.setTextColor(resources.getColor(R.color.debit_color))
                }

                cardItemRowCust.clickWithDebounce {

                    cardItemRowCust.context.startActivity(
                        Intent(cardItemRowCust.context, MetalColoursDetailsActivity ::class.java)
                            .putExtra(Constants.METALCOLOUR_DETAIL_KEY, Gson().toJson(metalColourModel))
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_all_item, parent, false))

    override fun getItemCount(): Int = metalColourList.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(metalColourList[position])
    }

    fun addMetalColour(
        metalColour: List<MetalColourModel.DataMetalColour>?
    ) {
        this.metalColourList.apply {
            clear()
            if (metalColour != null) {
                addAll(metalColour)
            }

        }
    }
}

