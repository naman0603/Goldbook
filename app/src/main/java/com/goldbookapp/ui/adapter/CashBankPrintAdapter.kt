package com.goldbookapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.CashBankPrintModel
import com.goldbookapp.model.StockPrintModel
import com.goldbookapp.utils.Constants
import kotlinx.android.synthetic.main.row_cashbankbook_report.view.*

class CashBankPrintAdapter(
    private val transactionsList: List<CashBankPrintModel.Data.Ledgers>?,
    private val categories: List<StockPrintModel.Data.Categories>?,
    private val ledgerList: List<CashBankPrintModel.Data.Ledgers>?,
    private val entryFrom: String             //1 -> cashbank 2-> stock ->3-ledger
) : RecyclerView.Adapter<CashBankPrintAdapter.DataViewHolder>(){

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var adapter: ContactReportPrintAdapter

        fun bind(
            itemModel: CashBankPrintModel.Data.Ledgers?,
            category: StockPrintModel.Data.Categories?,
            ledger: CashBankPrintModel.Data.Ledgers?,
            entryFrom: String
        ) {
            itemView.apply {
                when(entryFrom){
                    "1" -> {
                        // cashbank
                        cbr_tv_cash_ledger_name.text = itemModel!!.ledger_name
                        cbr_ll_total.visibility = View.VISIBLE
                        cbr_ll_subtotal.visibility = View.GONE
                        rv_cashbank_report.layoutManager = LinearLayoutManager(context)
                        adapter = ContactReportPrintAdapter(null,null, itemModel.dates, null, "3")
                        rv_cashbank_report.adapter = adapter


                        when (itemModel.total_amount){
                            "0.00" -> {
                                cbr_tv_totalvalue.text = "${Constants.AMOUNT_RS_APPEND}${itemModel.total_amount} ${itemModel.total_amount_term}"
                                cbr_tv_totalvalue.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.header_black_text
                                    )
                                )
                            }

                            else -> {
                                cbr_tv_totalvalue.text = Constants.AMOUNT_RS_APPEND+itemModel.total_amount + " " + if(itemModel.total_amount_term.equals("Dr", ignoreCase = true)) "Dr" else "Cr"
                                if (cbr_tv_totalvalue.text.contains("Dr", ignoreCase = true)) {
                                    cbr_tv_totalvalue.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.debit_color
                                        )
                                    )
                                } else
                                    cbr_tv_totalvalue.setTextColor(
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
                        cbr_ll_total.visibility = View.GONE
                        cbr_ll_subtotal.visibility = View.VISIBLE
                        cbr_tv_cash_ledger_name.text = category!!.category
                        rv_cashbank_report.layoutManager = LinearLayoutManager(context)
                        adapter = ContactReportPrintAdapter(null,null, null, category.items, "4")
                        rv_cashbank_report.adapter = adapter

                        cbr_tv_subtotal_nw.text = "${Constants.NETWT_APPEND}${category.sub_total_net_wt}"
                        cbr_tv_subtotal_fw.text = "${Constants.FINEWT_APPEND}${category.sub_total_fine_wt}"
                    }

                    "3" -> {
                        // ledger
                        cbr_tv_cash_ledger_name.text = ledger!!.ledger_name
                        cbr_ll_total.visibility = View.VISIBLE
                        cbr_ll_subtotal.visibility = View.GONE
                        rv_cashbank_report.layoutManager = LinearLayoutManager(context)
                        adapter = ContactReportPrintAdapter(null,null, ledger.dates, null, "5")
                        rv_cashbank_report.adapter = adapter


                        when (ledger.total_amount){
                            "0.00" -> {
                                cbr_tv_totalvalue.text = "${Constants.AMOUNT_RS_APPEND}${ledger.total_amount} ${ledger.total_amount_term}"
                                cbr_tv_totalvalue.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.header_black_text
                                    )
                                )
                            }

                            else -> {
                                cbr_tv_totalvalue.text = Constants.AMOUNT_RS_APPEND+ledger.total_amount + " " + if(ledger.total_amount_term.equals("Dr", ignoreCase = true)) "Dr" else "Cr"
                                if (cbr_tv_totalvalue.text.contains("Dr", ignoreCase = true)) {
                                    cbr_tv_totalvalue.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.debit_color
                                        )
                                    )
                                } else
                                    cbr_tv_totalvalue.setTextColor(
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
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_cashbankbook_report, parent, false))

    override fun getItemCount(): Int = when(entryFrom) {
        "1" -> transactionsList!!.size
        "2" -> categories!!.size
        "3" -> ledgerList!!.size
        else -> 0
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        when (entryFrom) {
            "1" -> holder.bind(this.transactionsList!![position], null,null, "1")
            "2" -> holder.bind(null, categories!![position],null, "2")
            "3" -> holder.bind(null,null, this.ledgerList!![position],"3")
        }

    }


}

