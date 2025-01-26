package com.goldbookapp.ui.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.SearchListPurchaseModel
import com.goldbookapp.model.SearchListSalesModel
import com.goldbookapp.ui.activity.sales.SalesBillDetailActivity
import com.goldbookapp.utils.BaseViewHolderSP
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_sales.view.*

class SalesListAdapter(
    private val salesList: ArrayList<SearchListSalesModel.Data1465085328>,
    private var totalPage: Int
) : RecyclerView.Adapter<BaseViewHolderSP>() {
    companion object {

        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_NORMAL = 1
        private var isLoaderVisible = false
        private var viewDetail: Boolean = false
    }

    class ViewHolder internal constructor(itemView: View?) : BaseViewHolderSP(itemView) {
        override fun clear() {}
        override fun onBind(salesModel: SearchListSalesModel.Data1465085328) {
            bind(salesModel)
        }

        override fun onBind(dataPurchase: SearchListPurchaseModel.DataPurchase) {

        }


        fun bind(salesModel: SearchListSalesModel.Data1465085328) {
            itemView.apply {
                tv_sl_firstRow_Custname.text = salesModel.contact_name
                tv_sl_secondRow_invoice_no.text = salesModel.invoice_number
                tv_sl_secondRow_transactiondate.text = salesModel.transaction_date
                tv_sl_thirdRow_noofitems.text = salesModel.total_items.toString()+ " item"
                tv_sl_thirdRow_total_fine_wt.text =
                    "Total " + salesModel.total_fine_wt.toString() + " fine wt."
                /* if (salesModel.signature_verify?.toString()?.contains("1")!!) {
                     tv_sl_firstRow_CustVerificaton.text = "Verified"
                     tv_sl_firstRow_CustVerificaton.setTextColor(
                         ContextCompat.getColor(
                             context,
                             R.color.green
                         )
                     )
                 } else {
                     tv_sl_firstRow_CustVerificaton.text = "Not Verified"
                     tv_sl_firstRow_CustVerificaton.setTextColor(
                         ContextCompat.getColor(
                             context,
                             R.color.red
                         )
                     )
                 }*/

                //tvLeftBalRowCust.text = customerModel.fine_limit
                //tvRightBalRowCust.text = customerModel.cash_limit
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
                                    SalesBillDetailActivity::class.java
                                )
                                    .putExtra(Constants.SALES_DETAIL_KEY, Gson().toJson(salesModel))
                            )
                        }
                        else->{

                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolderSP {
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

    override fun getItemCount(): Int = salesList.size

    override fun onBindViewHolder(holder: BaseViewHolderSP, position: Int) {
        holder.onBind(salesList[position])
    }

    fun addLoading() {
        isLoaderVisible = true

        notifyItemInserted(salesList.size - 1)
    }

    fun removeLoading() {
        isLoaderVisible = false

        notifyItemRemoved(salesList.size - 1)
        // }
    }

    fun clear() {
        salesList.clear()
        notifyDataSetChanged()
    }

    class ProgressHolder internal constructor(itemView: View?) : BaseViewHolderSP(itemView) {
        override fun clear() {

        }

        override fun onBind(data1465085328: SearchListSalesModel.Data1465085328) {

        }

        override fun onBind(dataPurchase: SearchListPurchaseModel.DataPurchase) {

        }


    }

    override fun getItemViewType(position: Int): Int {
        when (totalPage > 1) {
            true -> {
                return if (isLoaderVisible) {
                    if (position == salesList.size - 1) VIEW_TYPE_LOADING else VIEW_TYPE_NORMAL
                } else {
                    VIEW_TYPE_NORMAL
                }
            }
            false -> {
                return VIEW_TYPE_NORMAL
            }
        }

    }

    fun addSale(
        salesList: List<SearchListSalesModel.Data1465085328>?,
        fromSearch: Boolean,
        pageSize: Int,
        currentPage: Int,
        totalPage: Int
    ) {

        this.totalPage = totalPage
        if ((fromSearch && this.salesList.size <= pageSize) && !(currentPage > 1) /*&& totalPage == 1*/) {
            this.salesList.clear()

        }
        this.salesList.apply {
            //clear()
            if (salesList != null) {
                addAll(salesList)
            }
        }

    }

    fun viewDetail(viewdetail: Boolean) {
        viewDetail = viewdetail
    }
}

