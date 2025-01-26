package com.goldbookapp.ui.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.SearchListOpeningStockModel
import com.goldbookapp.ui.activity.openingstock.OpeningStockDetailsActivity
import com.goldbookapp.utils.BaseViewHolderOpeningStock
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_sales.view.*

class OpeningStockListAdapter(
    private val openingStockList: ArrayList<SearchListOpeningStockModel.DataOpeningStock>,
    private var totalPage: Int
) : RecyclerView.Adapter<BaseViewHolderOpeningStock>() {
    companion object {

        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_NORMAL = 1
        private var isLoaderVisible = false
        private var viewDetail: Boolean = false
    }

    class ViewHolder internal constructor(itemView: View?) : BaseViewHolderOpeningStock(itemView) {

        fun bind(openingStockModel: SearchListOpeningStockModel.DataOpeningStock) {
            itemView.apply {
                tv_sl_firstRow_Custname.visibility = View.GONE
                tv_sl_secondRow_invoice_no.text = openingStockModel.invoice_number
                tv_sl_secondRow_transactiondate.text = openingStockModel.transaction_date
                tv_sl_thirdRow_noofitems.text = openingStockModel.quantity.toString() + " item"
                tv_sl_thirdRow_total_fine_wt.text =
                    "Total " + openingStockModel.fine_wt.toString() + " fine wt."


                ll_row_sale.setOnFocusChangeListener { view, b ->
                    CommonUtils.hideKeyboardnew(
                        ll_row_sale.context as Activity
                    )
                }

                cardSalesItemRowSale.clickWithDebounce {
                    when (viewDetail) {
                        true -> {
                            cardSalesItemRowSale.context.startActivity(
                                Intent(
                                    cardSalesItemRowSale.context,
                                    OpeningStockDetailsActivity::class.java
                                )
                                    .putExtra(
                                        Constants.OPENING_STOCK_DETAIL_KEY,
                                        Gson().toJson(openingStockModel)
                                    )
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



        override fun onBind(dataOpeningStock: SearchListOpeningStockModel.DataOpeningStock) {
            bind(dataOpeningStock)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolderOpeningStock {
        return when (viewType) {
            VIEW_TYPE_NORMAL -> ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_sales, parent, false)
            )
            VIEW_TYPE_LOADING -> ProgressHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
            )
            else -> throw IllegalArgumentException("Different View Type")
        }
    }

    override fun getItemCount(): Int = openingStockList.size

    override fun onBindViewHolder(holder: BaseViewHolderOpeningStock, position: Int) {
        holder.onBind(openingStockList[position])
    }

    fun addLoading() {
        isLoaderVisible = true

        notifyItemInserted(openingStockList.size - 1)
    }

    fun removeLoading() {
        isLoaderVisible = false

        notifyItemRemoved(openingStockList.size - 1)

    }

    fun clear() {
        openingStockList.clear()
        notifyDataSetChanged()
    }

    class ProgressHolder internal constructor(itemView: View?) : BaseViewHolderOpeningStock(itemView) {
        override fun clear() {

        }



        override fun onBind(dataPurchase: SearchListOpeningStockModel.DataOpeningStock) {

        }


    }

    override fun getItemViewType(position: Int): Int {
        when (totalPage > 1) {
            true -> {
                return if (isLoaderVisible) {
                    if (position == openingStockList.size - 1) VIEW_TYPE_LOADING else VIEW_TYPE_NORMAL
                } else {
                    VIEW_TYPE_NORMAL
                }
            }
            false -> {
                return VIEW_TYPE_NORMAL
            }
        }

    }


    fun addOpeningStock(
        openingStockList: List<SearchListOpeningStockModel.DataOpeningStock>?,
        fromSearch: Boolean,
        pageSize: Int,
        currentPage: Int,
        totalPage: Int
    ) {
        this.totalPage = totalPage
        if ((fromSearch && this.openingStockList.size <= pageSize) && !(currentPage > 1) /*&& totalPage == 1*/) {
            this.openingStockList.clear()

        }
        this.openingStockList.apply {
            //clear()
            if (openingStockList != null) {
                addAll(openingStockList)
            }
        }

    }

    fun viewDetail(viewdetail: Boolean) {
        viewDetail = viewdetail
    }


}

