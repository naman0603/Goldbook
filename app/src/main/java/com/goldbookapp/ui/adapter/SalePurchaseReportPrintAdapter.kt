package com.goldbookapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.ContactPrintModel
import com.goldbookapp.model.SalesPurchasePrintModel
import com.goldbookapp.utils.Constants
import kotlinx.android.synthetic.main.row_salepurchase_trans.view.*

class SalePurchaseReportPrintAdapter(
    private val transactionsList: List<SalesPurchasePrintModel.Data.ReportData>?
) : RecyclerView.Adapter<SalePurchaseReportPrintAdapter.DataViewHolder>(){

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        private lateinit var adapter: SalePurchaseReportPrintSubAdapter

        fun bind(
            itemModel: SalesPurchasePrintModel.Data.ReportData,
            position: Int
        ) {
            itemView.apply {
                spr_tv_contactname.text = itemModel.customer_name

                row_cbr_trans_firstcolumn.text = "${Constants.NETWT_APPEND}${itemModel.sub_total_net_wt}"
                row_cbr_trans_secondcolumn.text = "${Constants.FINEWT_APPEND}${itemModel.sub_total_fine_wt}"
                row_cbr_trans_thirdcolumn.text = "${Constants.AMOUNT_RS_APPEND} ${itemModel.sub_total_amount}"

                // set transaction adapter
                spr_rv_transactions.layoutManager = LinearLayoutManager(context)
                adapter = SalePurchaseReportPrintSubAdapter(itemModel.entries as ArrayList<SalesPurchasePrintModel.Data.Entries>)
                spr_rv_transactions.adapter = adapter

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_salepurchase_trans, parent, false))


    override fun getItemCount(): Int = transactionsList!!.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(this.transactionsList!![position],position)
    }


}

