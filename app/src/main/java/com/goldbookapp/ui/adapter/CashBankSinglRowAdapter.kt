package com.goldbookapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.CashBankPrintModel
import com.goldbookapp.model.StockPrintModel
import com.goldbookapp.utils.Constants
import kotlinx.android.synthetic.main.row_cashbank_subtrans.view.*

class CashBankSinglRowAdapter(
    private val transactionsList: List<CashBankPrintModel.Data.Ledgers.Dates.Entries>?,
    private val itemEntries: List<StockPrintModel.Data.Categories.Items.Entries>?,
    private val ledgerEntries: List<CashBankPrintModel.Data.Ledgers.Dates.Entries>?,
    private val entryFrom : String?
) : RecyclerView.Adapter<CashBankSinglRowAdapter.DataViewHolder>(){

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            itemModel: CashBankPrintModel.Data.Ledgers.Dates.Entries?,
            itemEntry: StockPrintModel.Data.Categories.Items.Entries?,
            ledgerEntry: CashBankPrintModel.Data.Ledgers.Dates.Entries?,
            entryFrom: String?
        ) {
            itemView.apply {
                when(entryFrom){
                    "1" -> {
                        //cashbank

                        row_cbr_subtrans_firstcolumn.text = itemModel!!.customer_name
                        row_cbr_subtrans_secondcolumn.text = itemModel.series
                        when (itemModel.amount){
                            "0.00" -> {
                                row_cbr_subtrans_thirdcolumn.text = "${itemModel.amount} ${itemModel.amount_term}"
                                row_cbr_subtrans_thirdcolumn.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.header_black_text
                                    )
                                )
                            }

                            else -> {
                                row_cbr_subtrans_thirdcolumn.text = itemModel.amount + " " + if(itemModel.amount_term.equals("Dr", ignoreCase = true)) "Dr" else "Cr"
                                if (row_cbr_subtrans_thirdcolumn.text.contains("Dr", ignoreCase = true)) {
                                    row_cbr_subtrans_thirdcolumn.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.debit_color
                                        )
                                    )
                                } else
                                    row_cbr_subtrans_thirdcolumn.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.credit_color
                                        )
                                    )
                            }
                        }
                    }
                    "2" -> {
                        //stock

                        row_cbr_subtrans_firstcolumn.text = "${Constants.NETWT_APPEND}${itemEntry!!.net_wt}"
                        row_cbr_subtrans_secondcolumn.text = "${Constants.TOUCH_APPEND}${itemEntry.touch}"
                        row_cbr_subtrans_thirdcolumn.text = "${Constants.FINEWT_APPEND}${itemEntry.fine_wt}"
                    }
                    "3" -> {
                        //ledger

                        row_cbr_subtrans_firstcolumn.text = ledgerEntry!!.series
                        row_cbr_subtrans_secondcolumn.text = ledgerEntry!!.customer_name


                        when (ledgerEntry.amount){
                            "0.00" -> {
                                row_cbr_subtrans_thirdcolumn.text = "${ledgerEntry.amount} ${ledgerEntry.amount_term}"
                                row_cbr_subtrans_thirdcolumn.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.header_black_text
                                    )
                                )
                            }

                            else -> {
                                row_cbr_subtrans_thirdcolumn.text = ledgerEntry.amount + " " + if(ledgerEntry.amount_term.equals("Dr", ignoreCase = true)) "Dr" else "Cr"
                                if (row_cbr_subtrans_thirdcolumn.text.contains("Dr", ignoreCase = true)) {
                                    row_cbr_subtrans_thirdcolumn.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.debit_color
                                        )
                                    )
                                } else
                                    row_cbr_subtrans_thirdcolumn.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.credit_color
                                        )
                                    )
                            }
                        }
                    }
                }



            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_cashbank_subtrans, parent, false))

    override fun getItemCount(): Int = when(entryFrom){
        "1" -> this.transactionsList!!.size
        "2" -> this.itemEntries!!.size
        "3" -> this.ledgerEntries!!.size
        else -> 0
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        when(entryFrom){
            "1" -> holder.bind(this.transactionsList!![position],null,null, "1")
            "2" -> holder.bind(null,this.itemEntries!![position],null,"2")
            "3" -> holder.bind(null,null,this.ledgerEntries!![position],"3")
        }

    }


}

