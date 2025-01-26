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
import com.goldbookapp.ui.activity.purchase.PurchaseBillDetailActivity
import com.goldbookapp.utils.BaseViewHolderSP
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_sales.view.*

class PurchaseListAdapter(
    private val purchaseList: ArrayList<SearchListPurchaseModel.DataPurchase>,
    private var totalPage: Int
) : RecyclerView.Adapter<BaseViewHolderSP>() {
    companion object {

        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_NORMAL = 1
        private var isLoaderVisible = false
        private var viewDetail: Boolean = false
    }

    class ViewHolder internal constructor(itemView: View?) : BaseViewHolderSP(itemView) {

        fun bind(purchaseModel: SearchListPurchaseModel.DataPurchase) {
            itemView.apply {
                tv_sl_firstRow_Custname.text = purchaseModel.contact_name
                tv_sl_secondRow_invoice_no.text = purchaseModel.invoice_number
                tv_sl_secondRow_transactiondate.text = purchaseModel.transaction_date
                tv_sl_thirdRow_noofitems.text = purchaseModel.total_items.toString() + " item"
                tv_sl_thirdRow_total_fine_wt.text =
                    "Total " + purchaseModel.total_fine_wt.toString() + " fine wt."
                /*if (purchaseModel.signature_verify?.toString()?.contains("1")!!) {
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
                            R.color.green
                        )
                    )
                }*/

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
                                    PurchaseBillDetailActivity::class.java
                                )
                                    .putExtra(
                                        Constants.PURCHASE_DETAIL_KEY,
                                        Gson().toJson(purchaseModel)
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

        override fun onBind(data1465085328: SearchListSalesModel.Data1465085328) {
            //
        }

        override fun onBind(dataPurchase: SearchListPurchaseModel.DataPurchase) {
            bind(dataPurchase)
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

    override fun getItemCount(): Int = purchaseList.size

    override fun onBindViewHolder(holder: BaseViewHolderSP, position: Int) {
        holder.onBind(purchaseList[position])
    }

    fun addLoading() {
        isLoaderVisible = true

        notifyItemInserted(purchaseList.size - 1)
    }

    fun removeLoading() {
        isLoaderVisible = false

        notifyItemRemoved(purchaseList.size - 1)

    }

    fun clear() {
        purchaseList.clear()
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
                    if (position == purchaseList.size - 1) VIEW_TYPE_LOADING else VIEW_TYPE_NORMAL
                } else {
                    VIEW_TYPE_NORMAL
                }
            }
            false -> {
                return VIEW_TYPE_NORMAL
            }
        }

    }


    fun addPurchase(
        purchaseList: List<SearchListPurchaseModel.DataPurchase>?,
        fromSearch: Boolean,
        pageSize: Int,
        currentPage: Int,
        totalPage: Int
    ) {
        this.totalPage = totalPage
        if ((fromSearch && this.purchaseList.size <= pageSize) && !(currentPage > 1) /*&& totalPage == 1*/) {
            this.purchaseList.clear()

        }
        this.purchaseList.apply {
            //clear()
            if (purchaseList != null) {
                addAll(purchaseList)
            }
        }

    }

    fun viewDetail(viewdetail: Boolean) {
        viewDetail = viewdetail
    }
}

