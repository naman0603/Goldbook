package com.goldbookapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.CashBankPrintModel
import com.goldbookapp.model.ContactPrintModel
import com.goldbookapp.model.DayPrintModel
import com.goldbookapp.model.StockPrintModel
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.Constants
import kotlinx.android.synthetic.main.row_contact_trans.view.*

class ContactReportPrintAdapter(
    private val transactionsList: List<ContactPrintModel.Data.ReportData.Transactions>?, // 1 -> contact,2-> day, 3->cashbank, 4-> stock 5->ledger
    private val dayLineEntries: List<DayPrintModel.Data.Line_enries.Entries>?,
    private val cashbankEntries: List<CashBankPrintModel.Data.Ledgers.Dates>?,
    private val items: List<StockPrintModel.Data.Categories.Items>?,
    private val entryFrom: String
) : RecyclerView.Adapter<ContactReportPrintAdapter.DataViewHolder>(){

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bind(
            itemModel: ContactPrintModel.Data.ReportData.Transactions?,
            dayLineEntries: DayPrintModel.Data.Line_enries.Entries?,
            cashbankEntries: CashBankPrintModel.Data.Ledgers.Dates?,
            items: StockPrintModel.Data.Categories.Items?,
            entryFrom: String
        ) {
            itemView.apply {
                when(entryFrom){
                    "1" -> {
                        //contact
                        ll_contact_trans_row1.visibility = View.VISIBLE
                        contact_trans_rv_cashbankentries.visibility = View.GONE
                        var seriesItemNameStringBuilder: java.lang.StringBuilder = java.lang.StringBuilder()
                        seriesItemNameStringBuilder
                            .append(itemModel!!.series!!.trim()).append(", ")
                            .append(itemModel.item_name!!.trim())

                        contact_trans_row1_column1.text = CommonUtils.removeUnwantedComma(seriesItemNameStringBuilder.toString())
                        contact_trans_row2_column1.text = itemModel.transaction_date
                        contact_trans_row2_column2.text = "${Constants.FINEWT_APPEND}${itemModel.fine_wt}"
                        contact_trans_row2_column3.text = "${Constants.AMOUNT_RS_APPEND} ${itemModel.amount}"
                    }
                    "2" -> {
                        //day
                        ll_contact_trans_row1.visibility = View.VISIBLE
                        contact_trans_rv_cashbankentries.visibility = View.GONE
                        contact_trans_row1_column1.text = dayLineEntries!!.customer_name
                        contact_trans_row2_column1.text = dayLineEntries.series

                        // qty set with default term
                        when (dayLineEntries.qty){
                            "0.000" -> {
                                contact_trans_row2_column3.text = "${dayLineEntries.qty} ${dayLineEntries.qty_term}"
                                contact_trans_row2_column3.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.header_black_text
                                    )
                                )
                            }

                            else -> {
                                contact_trans_row2_column3.text = dayLineEntries.qty + " " + if(dayLineEntries.qty_term.equals("Dr", ignoreCase = true)) "In" else "Out"
                                if (contact_trans_row2_column3.text.contains("In", ignoreCase = true)) {
                                    contact_trans_row2_column3.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.debit_color
                                        )
                                    )
                                } else
                                    contact_trans_row2_column3.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.credit_color
                                        )
                                    )
                            }
                        }
                        // amount set with default term
                        when (dayLineEntries.amount){
                            "0.00" -> {
                                contact_trans_row2_column2.text = "${Constants.AMOUNT_RS_APPEND}${dayLineEntries.amount} ${dayLineEntries.amount_term}"
                                contact_trans_row2_column2.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.header_black_text
                                    )
                                )
                            }

                            else -> {
                                contact_trans_row2_column2.text = Constants.AMOUNT_RS_APPEND+dayLineEntries.amount + " " + if(dayLineEntries.amount_term.equals("Dr", ignoreCase = true)) "Dr" else "Cr"
                                if (contact_trans_row2_column2.text.contains("Dr", ignoreCase = true)) {
                                    contact_trans_row2_column2.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.debit_color
                                        )
                                    )
                                } else
                                    contact_trans_row2_column2.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.credit_color
                                        )
                                    )
                            }
                        }
                    }
                    "3" -> {
                        //cashbank
                        ll_contact_trans_row1.visibility = View.GONE
                        contact_trans_row1_column1.text = cashbankEntries!!.date
                        contact_trans_rv_cashbankentries.visibility = View.VISIBLE
                        contact_trans_rv_cashbankentries.layoutManager = LinearLayoutManager(context)
                        val adapter =
                        CashBankSinglRowAdapter(cashbankEntries.entries,null,null,"1")
                        contact_trans_rv_cashbankentries.adapter = adapter
                    }
                    "4" -> {
                        // stock
                        ll_contact_trans_row1.visibility = View.GONE
                        contact_trans_row1_column1.text = items!!.item
                        contact_trans_rv_cashbankentries.visibility = View.VISIBLE
                        contact_trans_rv_cashbankentries.layoutManager = LinearLayoutManager(context)
                        val adapter =
                            CashBankSinglRowAdapter(null,items.entries,null,"2")
                        contact_trans_rv_cashbankentries.adapter = adapter
                    }
                    "5" -> {
                        //ledger
                        ll_contact_trans_row1.visibility = View.GONE
                        contact_trans_row1_column1.text = cashbankEntries!!.date
                        contact_trans_rv_cashbankentries.visibility = View.VISIBLE
                        contact_trans_rv_cashbankentries.layoutManager = LinearLayoutManager(context)
                        val adapter =
                            CashBankSinglRowAdapter(null,null,cashbankEntries.entries,"3")
                        contact_trans_rv_cashbankentries.adapter = adapter
                    }
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_contact_trans, parent, false))

    override fun getItemCount(): Int = when(entryFrom) {
        "1" -> transactionsList!!.size
        "2" -> dayLineEntries!!.size
        "3" -> cashbankEntries!!.size
        "4" -> items!!.size
        "5" -> cashbankEntries!!.size
        else -> 0
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        when(entryFrom){
            "1" ->  holder.bind(this.transactionsList!![position], null,null,null,"1")
            "2" ->  holder.bind(null, dayLineEntries!![position], null, null,"2")
            "3" -> holder.bind(null, null, cashbankEntries!![position], null,"3")
            "4" -> holder.bind(null, null, null, items!![position], "4")
            "5" -> holder.bind(null, null, cashbankEntries!![position], null, "5")

        }
    }


}

