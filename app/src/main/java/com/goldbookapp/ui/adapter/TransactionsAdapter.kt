package com.goldbookapp.ui.adapter

import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.TransactionHistoryModel
import com.goldbookapp.ui.activity.payment.PaymentDetailActivity
import com.goldbookapp.ui.activity.purchase.PurchaseBillDetailActivity
import com.goldbookapp.ui.activity.receipt.ReceiptDetailActivity
import com.goldbookapp.ui.activity.sales.SalesBillDetailActivity
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import kotlinx.android.synthetic.main.row_transactions.view.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class TransactionsAdapter(private val transactions: ArrayList<TransactionHistoryModel.Data>,
                          private var totalPage: Int) : RecyclerView.Adapter<TransactionsAdapter.DataViewHolder>(){

    private var isLoaderVisible = false
    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(transactions: TransactionHistoryModel.Data) {
            itemView.apply {
                var datestring = transactions.created_date
                var timestring = transactions.created_time

                if(!datestring.equals("")){
                    val dateformatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        //source format
                        DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.ENGLISH)
                    } else {
                        TODO("VERSION.SDK_INT < O")
                    }
                    var date = LocalDate.parse(datestring, dateformatter)


                    var dateformatt = DateTimeFormatter.ofPattern("dd-MMM-yy")
                    var formattedDate = date.format(dateformatt)



                    val timeparser = SimpleDateFormat("hh:mm:ss a")
                    val timeformatter = SimpleDateFormat("hh:mm a")
                    var formattedtime = timeformatter.format(timeparser.parse(timestring))
                    // if(transactions.ledger_type.equals("credit",true)){

                    //tvSalesDateTransaction.setText(formattedDate.toString() + "  |  " + formattedtime )
                    tvSalesDateTransaction.setText(formattedDate.toString())
                    tvSalesTimeTransaction.setText(formattedtime.toString())
                }else{
                    tvSalesDateTransaction.setText("")
                }


                tvSalesVCTransaction.text = transactions.transaction_type
                    if(transactions.total_net_wt!=null){
                        tvSalesTotalTransaction.text = transactions.total_net_wt
                    }
                    if(transactions.transaction_number!=null){
                        tvSalesNumberTransaction.text = transactions.transaction_number.toString()
                    }
                    when(transactions.credit_fine_wt.toString()){
                        "0.000" -> {
                            /*tvReceiptInwardTransactionOne.text = transactions.credit_fine_wt.toString()
                            tvReceiptInwardTransactionOne.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.header_black_text))*/
                            tvReceiptInwardTransactionOne.text= " - "
                        }
                        else -> {
                            tvReceiptInwardTransactionOne.text = transactions.credit_fine_wt.toString()
                            tvReceiptInwardTransactionOne.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.inwardcolor))
                        }

                    }
                    when(transactions.credit_amount.toString()){
                        "0.00" -> {
                           /* tvReceiptInwardTransactionTwo.text = transactions.credit_amount.toString()
                            tvReceiptInwardTransactionTwo.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.header_black_text))*/
                            tvReceiptInwardTransactionTwo.text =" - "
                        }
                        else -> {
                            tvReceiptInwardTransactionTwo.text = transactions.credit_amount.toString()
                            tvReceiptInwardTransactionTwo.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.inwardcolor))
                        }

                    }

                    when(transactions.debit_fine_wt.toString()){
                        "0.000" -> {
                            /*tvSalesOutwardTransactionOne.text = transactions.debit_fine_wt.toString()
                            tvSalesOutwardTransactionOne.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.header_black_text))*/
                            tvSalesOutwardTransactionOne.text =" - "
                        }
                        else -> {
                            tvSalesOutwardTransactionOne.text = transactions.debit_fine_wt.toString()
                            tvSalesOutwardTransactionOne.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.outwardcolor))
                        }

                    }
                    when(transactions.debit_amount.toString()){
                        "0.00" -> {
                            /*tvSalesOutwardTransactionTwo.text = transactions.debit_amount.toString()
                            tvSalesOutwardTransactionTwo.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.header_black_text))*/
                            tvSalesOutwardTransactionTwo.text =" - "
                        }
                        else -> {
                            tvSalesOutwardTransactionTwo.text = transactions.debit_amount.toString()
                            tvSalesOutwardTransactionTwo.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.outwardcolor))
                        }

                    }

            }
            itemView.clickWithDebounce {

                when(transactions.module){
                "sales"-> {
                    itemView.context.startActivity(Intent(itemView.context, SalesBillDetailActivity::class.java)
                        .putExtra(Constants.ModuleID, transactions.module_id))
                }
                "purchase" ->{
                    itemView.context.startActivity(Intent(itemView.context, PurchaseBillDetailActivity::class.java)
                        .putExtra(Constants.ModuleID, transactions.module_id))
                }
                "payment" ->{
                    itemView.context.startActivity(Intent(itemView.context, PaymentDetailActivity::class.java)
                        .putExtra(Constants.ModuleID, transactions.module_id))
                }
                 "receipt" ->{
                     itemView.context.startActivity(Intent(itemView.context, ReceiptDetailActivity::class.java)
                         .putExtra(Constants.ModuleID, transactions.module_id))
                 }
                else ->{
                    // nothing to do
                }

            } }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_transactions, parent, false))

    override fun getItemCount(): Int = transactions.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    fun addLoading() {
        isLoaderVisible = true

        notifyItemInserted(transactions.size - 1)
    }

    fun removeLoading() {
        isLoaderVisible = false

        notifyItemRemoved(transactions.size - 1)
        // }
    }

    fun addtransactions(transactionsList: List<TransactionHistoryModel.Data>?,
                        pageSize: Int,
                        currentPage: Int,
                        totalPage: Int) {

        this.totalPage = totalPage
        if ( transactionsList!!.size <= pageSize && !(currentPage > 1) ) {
         //   transactionsList.clear()
            transactions.clear()

        }
        this.transactions.apply {
           // clear()
            if (transactionsList != null) {
                addAll(transactionsList)
            }
        }

    }
}

