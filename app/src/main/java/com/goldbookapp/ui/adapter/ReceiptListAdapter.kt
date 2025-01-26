package com.goldbookapp.ui.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.receipt.ReceiptDetailActivity
import com.goldbookapp.utils.BaseViewHolderPR
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_payment.view.*

class ReceiptListAdapter(
    private val receiptList: ArrayList<SearchListReceipt.DataReceipt>,
    private var totalPage: Int
) : RecyclerView.Adapter<BaseViewHolderPR>() {
    companion object {

        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_NORMAL = 1
        private var isLoaderVisible = false
        private var viewDetail: Boolean = false
    }

    class ViewHolder internal constructor(itemView: View?) : BaseViewHolderPR(itemView) {

        fun bind(receiptModel: SearchListReceipt.DataReceipt) {
            itemView.apply {
                tvNameRowPayment.text = receiptModel.contact_name
                tvPayRowPayment.text = receiptModel.invoice_number
                tvDateRowPayment.text = receiptModel.transaction_date
                if(receiptModel.total_fine_wt.equals("")){
                    tvAmountRowPayment.text = "Total " + "0.000" + " fine wt."
                }else{
                    tvAmountRowPayment.text = "Total " + receiptModel.total_fine_wt.toString() + " fine wt."
                }

                tvFineWtRowPayment.text =receiptModel.total_items.toString() + " item"


                ll_row_payment.setOnFocusChangeListener { view, b ->
                    CommonUtils.hideKeyboardnew(
                        ll_row_payment.context as Activity
                    )
                }

                cardPaymentList.clickWithDebounce {
                    when (viewDetail) {
                        true -> {
                            cardPaymentList.context.startActivity(
                                Intent(cardPaymentList.context, ReceiptDetailActivity::class.java)
                                    .putExtra(
                                        Constants.RECEIPT_DETAIL_KEY,
                                        Gson().toJson(receiptModel)
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
        }

        override fun onBind(dataPayment: SearchListPayment.DataPayment) {
        }

        override fun onBind(dataReceipt: SearchListReceipt.DataReceipt) {
            bind(dataReceipt)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolderPR {

        return when (viewType) {
            VIEW_TYPE_NORMAL -> ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_payment, parent, false)
            )
            VIEW_TYPE_LOADING -> ProgressHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
            )
            else -> throw IllegalArgumentException("Different View Type")
        }
    }

    override fun getItemCount(): Int = receiptList.size

    override fun onBindViewHolder(holder: BaseViewHolderPR, position: Int) {
        holder.onBind(receiptList[position])
    }

    fun addLoading() {
        isLoaderVisible = true
        notifyItemInserted(receiptList.size - 1)
    }

    fun removeLoading() {
        isLoaderVisible = false
        notifyItemRemoved(receiptList.size - 1)
    }

    fun clear() {
        receiptList.clear()
        notifyDataSetChanged()
    }

    class ProgressHolder internal constructor(itemView: View?) : BaseViewHolderPR(itemView) {
        override fun clear() {

        }

        override fun onBind(dataPayment: SearchListPayment.DataPayment) {
        }

        override fun onBind(dataReceipt: SearchListReceipt.DataReceipt) {

        }

    }

    override fun getItemViewType(position: Int): Int {
        when (totalPage > 1) {
            true -> {
                return if (isLoaderVisible) {
                    if (position == receiptList.size - 1) VIEW_TYPE_LOADING else VIEW_TYPE_NORMAL
                } else {
                    VIEW_TYPE_NORMAL
                }
            }
            false -> {
                return VIEW_TYPE_NORMAL
            }
        }

    }


    fun addReceipt(
        purchaseList: List<SearchListReceipt.DataReceipt>?,
        fromSearch: Boolean,
        pageSize: Int,
        currentPage: Int,
        totalPage: Int
    ) {
        this.totalPage = totalPage
        if ((fromSearch && this.receiptList.size <= pageSize) && !(currentPage > 1) /*&& totalPage == 1*/) {
            this.receiptList.clear()

        }
        this.receiptList.apply {
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

