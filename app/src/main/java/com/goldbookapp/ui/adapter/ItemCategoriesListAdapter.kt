package com.goldbookapp.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.ItemCategoryModel
import com.goldbookapp.ui.activity.settings.ItemCategoryDetailsActivity
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_all_item.view.*

class ItemCategoriesListAdapter(private val itemCatList: ArrayList<ItemCategoryModel.Data2101931085>) :
    RecyclerView.Adapter<ItemCategoriesListAdapter.DataViewHolder>() {

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(itemCatModel: ItemCategoryModel.Data2101931085) {
            itemView.apply {
                tvNameRowAllItem.text = itemCatModel.category_name
                tvStockRowAllItem.visibility = View.VISIBLE
                lngrosswt.visibility = View.GONE
                if (itemCatModel.status?.toString().equals("1", true)!!) {

                    tvStockRowAllItem.text = context.getString(R.string.active)
                    tvStockRowAllItem.setTextColor(resources.getColor(R.color.credit_color))
                } else {
                    tvStockRowAllItem.text = context.getString(R.string.inactive)
                    tvStockRowAllItem.setTextColor(resources.getColor(R.color.debit_color))
                }

                cardItemRowCust.clickWithDebounce {

                    cardItemRowCust.context.startActivity(
                        Intent(cardItemRowCust.context, ItemCategoryDetailsActivity::class.java)
                            .putExtra(
                                Constants.ITEMCATEGORY_DETAIL_KEY,
                                Gson().toJson(itemCatModel)
                            )
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.row_all_item, parent, false)
        )

    override fun getItemCount(): Int = itemCatList.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(itemCatList[position])
    }

    fun addItemCategories(item: List<ItemCategoryModel.Data2101931085>?) {
        this.itemCatList.apply {
            clear()
            if (item != null) {
                addAll(item)
            }

        }
    }
}

