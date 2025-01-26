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
import com.goldbookapp.ui.activity.payment.PaymentDetailActivity
import com.goldbookapp.utils.BaseViewHolderPR
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_payment.view.*


class PaymentListAdapter(
    private val paymentList: ArrayList<SearchListPayment.DataPayment>,
    private var totalPage: Int
) : RecyclerView.Adapter<BaseViewHolderPR>() {
    companion object {

        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_NORMAL = 1
        private var isLoaderVisible = false
        private var viewDetail: Boolean = false
    }

    class ViewHolder internal constructor(itemView: View?) : BaseViewHolderPR(itemView) {

        fun bind(paymentModel: SearchListPayment.DataPayment) {
            itemView.apply {
                tvNameRowPayment.text = paymentModel.contact_name
                tvPayRowPayment.text = paymentModel.invoice_number
                tvDateRowPayment.text = paymentModel.transaction_date
                if(paymentModel.total_fine_wt.equals("")){
                    tvAmountRowPayment.text ="Total " + "0.000"+ " fine wt."
                }else{
                    tvAmountRowPayment.text ="Total " + paymentModel.total_fine_wt.toString() + " fine wt."
                }

                tvFineWtRowPayment.text = paymentModel.total_items.toString() + " item"

                //tvLeftBalRowCust.text = customerModel.fine_limit
                //tvRightBalRowCust.text = customerModel.cash_limit
                ll_row_payment.setOnFocusChangeListener { view, b ->
                    CommonUtils.hideKeyboardnew(
                        ll_row_payment.context as Activity
                    )
                }

                cardPaymentList.clickWithDebounce {
                    when (viewDetail) {
                        true -> {
                            cardPaymentList.context.startActivity(
                                Intent(cardPaymentList.context, PaymentDetailActivity::class.java)
                                    .putExtra(
                                        Constants.PAYMENT_DETAIL_KEY,
                                        Gson().toJson(paymentModel)
                                    )
                            )
                        }else->{

                    }
                    }
                }
            }
        }

        override fun clear() {

        }

        override fun onBind(dataPayment: SearchListPayment.DataPayment) {

            bind(dataPayment)
        }

        override fun onBind(dataReceipt: SearchListReceipt.DataReceipt) {

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

    override fun getItemCount(): Int = paymentList.size

    override fun onBindViewHolder(holder: BaseViewHolderPR, position: Int) {
        holder.onBind(paymentList[position])
    }

    fun addLoading() {
        isLoaderVisible = true

        notifyItemInserted(paymentList.size - 1)
    }

    fun removeLoading() {
        isLoaderVisible = false

        notifyItemRemoved(paymentList.size - 1)
        // }
    }

    fun clear() {
        paymentList.clear()
        notifyDataSetChanged()
    }

    class ProgressHolder internal constructor(itemView: View?) : BaseViewHolderPR(itemView) {
        override fun clear() {

        }

        override fun onBind(dataPayment: SearchListPayment.DataPayment) {
            // TODO("Not yet implemented")
        }

        override fun onBind(dataReceipt: SearchListReceipt.DataReceipt) {
            // TODO("Not yet implemented")
        }

    }

    override fun getItemViewType(position: Int): Int {
        when (totalPage > 1) {
            true -> {
                return if (isLoaderVisible) {
                    if (position == paymentList.size - 1) VIEW_TYPE_LOADING else VIEW_TYPE_NORMAL
                } else {
                    VIEW_TYPE_NORMAL
                }
            }
            false -> {
                return VIEW_TYPE_NORMAL
            }
        }

    }

    fun addPayment(
        purchaseList: List<SearchListPayment.DataPayment>?,
        fromSearch: Boolean,
        pageSize: Int,
        currentPage: Int,
        totalPage: Int
    ) {
        this.totalPage = totalPage
        if ((fromSearch && this.paymentList.size <= pageSize) && !(currentPage > 1) /*&& totalPage == 1*/) {
            this.paymentList.clear()

        }
        this.paymentList.apply {
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

