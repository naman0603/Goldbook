package com.goldbookapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.SalesPurchasePrintModel
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.Constants
import kotlinx.android.synthetic.main.row_subtransaction_salespurchase.view.*

class SalePurchaseReportPrintSubAdapter(private val subtransactionsList: ArrayList<SalesPurchasePrintModel.Data.Entries>) :
    RecyclerView.Adapter<SalePurchaseReportPrintSubAdapter.DataViewHolder>() {

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bind(
            subdata: SalesPurchasePrintModel.Data.Entries,
            position: Int
        ) {
            itemView.apply {
                var seriesItemNameStringBuilder: java.lang.StringBuilder = java.lang.StringBuilder()
                seriesItemNameStringBuilder
                    .append(subdata.series!!.trim()).append(", ")
                    .append(subdata.item_name!!.trim())

                spr_tv_column1.text =
                    CommonUtils.removeUnwantedComma(seriesItemNameStringBuilder.toString())
                spr_tv_column2.text = subdata.transaction_date
                spr_tv_row2_column1.text = "${Constants.NETWT_APPEND}${subdata.net_wt}"
                spr_tv_row2_column2.text = "${Constants.FINEWT_APPEND}${subdata.fine_wt}"
                spr_tv_row2_column3.text = "${Constants.AMOUNT_RS_APPEND} ${subdata.total_charges}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_subtransaction_salespurchase, parent, false)
        )

    override fun getItemCount(): Int = subtransactionsList.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(subtransactionsList[position], position)
    }

    fun addItem(transactionsList: List<SalesPurchasePrintModel.Data.Entries>?) {
        this.subtransactionsList.apply {
            clear()
            if (transactionsList != null) {
                addAll(transactionsList)
            }
        }

    }
}

