package com.goldbookapp.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.GetItemListModel
import com.goldbookapp.ui.activity.item.ItemDetailActivity
import com.goldbookapp.utils.BaseViewHolderItem
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_all_item.view.*

class ItemListAdapter(
    private val itemList: ArrayList<GetItemListModel.Data1077697879>,
    private var totalPage: Int
) : RecyclerView.Adapter<BaseViewHolderItem>() {
    companion object {

        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_NORMAL = 1
        private var isLoaderVisible = false
        private var viewDetail: Boolean = false
    }

    class ViewHolder internal constructor(itemView: View?) : BaseViewHolderItem(itemView) {

        fun bind(itemModel: GetItemListModel.Data1077697879) {
            itemView.apply {
                tvNameRowAllItem.text = itemModel.item_name
                tvStockRowAllItem.visibility = View.GONE

              //  var qty = itemModel.quantity
              //  if (qty == null) qty = "0"
                if(itemModel.item_type.equals("Service")){
                    lngrosswt.visibility = View.GONE
                }
                else{
                    lngrosswt.visibility = View.VISIBLE
                    tvGrossWtItem.text = "G.W.: " + itemModel.gross_wt + Constants.WEIGHT_GM_APPEND
                    tvNetWtItem.text = " N.W.: " + itemModel.net_wt + Constants.WEIGHT_GM_APPEND
                }

                //tvLeftBalRowCust.text = customerModel.fine_limit
                //tvRightBalRowCust.text = customerModel.cash_limit

                cardItemRowCust.clickWithDebounce {
                    when (viewDetail) {
                        true -> {
                            cardItemRowCust.context.startActivity(
                                Intent(cardItemRowCust.context, ItemDetailActivity::class.java)
                                    .putExtra(Constants.ITEM_DETAIL_KEY, Gson().toJson(itemModel))
                            )
                        }
                        else->{

                        }
                    }
                }
            }
        }

        override fun clear() {
            //
        }

        override fun onBind(data1077697879: GetItemListModel.Data1077697879) {
            bind(data1077697879)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolderItem {
        return when (viewType) {
            VIEW_TYPE_NORMAL -> ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_all_item, parent, false)
            )
            VIEW_TYPE_LOADING -> ProgressHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
            )
            else -> throw IllegalArgumentException("Different View Type")
        }
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: BaseViewHolderItem, position: Int) {
        holder.onBind(itemList[position])
    }

    fun addLoading() {
        isLoaderVisible = true

        notifyItemInserted(itemList.size - 1)
    }

    fun removeLoading() {
        isLoaderVisible = false

        notifyItemRemoved(itemList.size - 1)
        // }
    }

    fun clear() {
        itemList.clear()
        notifyDataSetChanged()
    }

    class ProgressHolder internal constructor(itemView: View?) : BaseViewHolderItem(itemView) {
        override fun clear() {

        }

        override fun onBind(data1077697879: GetItemListModel.Data1077697879) {
            //
        }


    }

    override fun getItemViewType(position: Int): Int {
        when (totalPage > 1) {
            true -> {
                return if (isLoaderVisible) {
                    if (position == itemList.size - 1) VIEW_TYPE_LOADING else VIEW_TYPE_NORMAL
                } else {
                    VIEW_TYPE_NORMAL
                }
            }
            false -> {
                return VIEW_TYPE_NORMAL
            }
        }

    }

    fun addItem(
        itemList: List<GetItemListModel.Data1077697879>?,
        fromSearch: Boolean,
        pageSize: Int,
        currentPage: Int,
        totalPage: Int
    ) {
        this.totalPage = totalPage
        if ((fromSearch && this.itemList.size <= pageSize) && !(currentPage > 1) /*&& totalPage == 1*/) {
            this.itemList.clear()

        }
        this.itemList.apply {
            //clear()
            if (itemList != null) {
                addAll(itemList)
            }
        }

    }

    fun viewDetail(viewdetail: Boolean) {
        viewDetail = viewdetail
    }
}

