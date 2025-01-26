package com.goldbookapp.ui.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.DashboardDetailsModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.activity.openingstock.OpeningStockDetailsActivity
import com.goldbookapp.ui.activity.payment.PaymentDetailActivity
import com.goldbookapp.ui.activity.purchase.PurchaseBillDetailActivity
import com.goldbookapp.ui.activity.receipt.ReceiptDetailActivity
import com.goldbookapp.ui.activity.sales.SalesBillDetailActivity
import com.goldbookapp.ui.ui.home.DashboardFragment
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_sales.view.*

class RecentTransactionAdapter(
    private var recentTransactionsList: ArrayList<DashboardDetailsModel.Data.Recent_transactions>?,
    private var permission: UserWiseRestrictionModel.Data,
    private val dashboardFragment : DashboardFragment
) : RecyclerView.Adapter<RecentTransactionAdapter.DataViewHolder>(){

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            recentTransaction: DashboardDetailsModel.Data.Recent_transactions,
            data: UserWiseRestrictionModel.Data,
            size: Int,
            dashboardFragment: DashboardFragment
        ) {
            itemView.apply {
                tv_sl_firstRow_Custname.text = recentTransaction.display_name
                tv_sl_secondRow_invoice_no.text=recentTransaction.transaction_number
                tv_sl_secondRow_transactiondate.text=recentTransaction.transaction_date
                tv_sl_thirdRow_noofitems.text= recentTransaction.no_of_items.toString()+" items"
                tv_sl_thirdRow_total_fine_wt.text= "Total " +recentTransaction.total_fine_wt.toString() +" fine wt."


                //tvLeftBalRowCust.text = customerModel.fine_limit
                //tvRightBalRowCust.text = customerModel.cash_limit
                ll_row_sale.setOnFocusChangeListener { view, b -> CommonUtils.hideKeyboardnew(
                    ll_row_sale.context as Activity
                ) }

                cardSalesItemRowSale.clickWithDebounce {

                    when(recentTransaction.module){
                        "sales"-> {
                            for (i in 0 until data.permission!!.size) {
                                if (data.permission!!.get(i).startsWith(context.getString(R.string.sale),true)) {
                                    when (data.permission!!.get(i).endsWith(context.getString(R.string.list), true)) {
                                        true -> {
                                            cardSalesItemRowSale.context.startActivity(Intent(cardSalesItemRowSale.context, SalesBillDetailActivity::class.java)
                                                //.putExtra(Constants.ModuleID, recentTransaction.module_id))
                                                .putExtra(
                                                    Constants.SALES_RECENT_TRANS_DETAIL_KEY,
                                                    Gson().toJson(recentTransaction)
                                                ))
                                        }else->{

                                    }
                                    }
                                }
                            }

                        }
                        "purchase" ->{
                            for (i in 0 until data.permission!!.size) {
                                if (data.permission!!.get(i).startsWith(context.getString(R.string.purchase),true)) {
                                    when (data.permission!!.get(i).endsWith(context.getString(R.string.list), true)) {
                                        true -> {
                                            cardSalesItemRowSale.context.startActivity(Intent(cardSalesItemRowSale.context, PurchaseBillDetailActivity::class.java)
                                              //  .putExtra(Constants.ModuleID, recentTransaction.module_id))
                                                .putExtra(
                                                    Constants.PURCHASE_RECENT_TRANS_DETAIL_KEY,
                                                    Gson().toJson(recentTransaction)
                                                ))
                                        }else->{

                                    }
                                    }
                                }
                            }

                        }
                        "payment" ->{
                            for (i in 0 until data.permission!!.size) {
                                if (data.permission!!.get(i).startsWith(context.getString(R.string.payment),true)) {
                                    when (data.permission!!.get(i).endsWith(context.getString(R.string.list), true)) {
                                        true -> {
                                            cardSalesItemRowSale.context.startActivity(Intent(cardSalesItemRowSale.context, PaymentDetailActivity::class.java)
                                              //  .putExtra(Constants.ModuleID, recentTransaction.module_id))
                                                .putExtra(
                                                    Constants.PAYMENT_RECENT_TRANS_DETAIL_KEY,
                                                    Gson().toJson(recentTransaction)
                                                ))
                                        }else->{

                                    }
                                    }
                                }
                            }

                        }
                        "receipt" ->{
                            for (i in 0 until data.permission!!.size) {
                                if (data.permission!!.get(i).startsWith(context.getString(R.string.receipt),true)) {
                                    when (data.permission!!.get(i).endsWith(context.getString(R.string.list), true)) {
                                        true -> {
                                            cardSalesItemRowSale.context.startActivity(Intent(cardSalesItemRowSale.context, ReceiptDetailActivity::class.java)
                                               // .putExtra(Constants.ModuleID, recentTransaction.module_id))
                                                .putExtra(
                                                    Constants.RECEIPT_RECENT_TRANS_DETAIL_KEY,
                                                    Gson().toJson(recentTransaction)
                                                ))
                                        }else->{

                                    }
                                    }
                                }
                            }

                        }
                        "opening stock"->{
                            cardSalesItemRowSale.context.startActivity(Intent(cardSalesItemRowSale.context, OpeningStockDetailsActivity::class.java)
                                 .putExtra(Constants.ModuleID, recentTransaction.module_id))

                        }
                        else ->{
                            // nothing to do
                        }

                    }


                }
                if ((adapterPosition + 1) == size) {
                    dashboardFragment.enableBtnsHideProgress()

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_sales, parent, false))

    override fun getItemCount(): Int = recentTransactionsList!!.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(this.recentTransactionsList!![position], permission, this.recentTransactionsList!!.size, dashboardFragment)
    }
    fun addRecentTransList(
        recentTransactionsList: List<DashboardDetailsModel.Data.Recent_transactions>,
        permission: UserWiseRestrictionModel.Data
    ) {
        this.recentTransactionsList.apply {
            this!!.clear()
            if (recentTransactionsList != null) {
                this.addAll(recentTransactionsList)
            }
        }
        this.permission = permission

    }


}

