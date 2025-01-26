package com.goldbookapp.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.TaxAnalysisListModel
import com.goldbookapp.ui.activity.settings.AddTaxAnalysisActivity
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_taxanalysis_list.view.*

class TaxAnalysisAdapter(
    private val mList: List<TaxAnalysisListModel.TaxAnalysisList>,
    private val isFromEdit: Boolean,
    private val transaction_type :String
) :
    RecyclerView.Adapter<TaxAnalysisAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_taxanalysis_list, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val ItemsViewModel = mList[position]


        holder.bind(mList[position], position, isFromEdit,transaction_type)

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        fun bind(
            itemViewModel: TaxAnalysisListModel.TaxAnalysisList,
            position: Int,
            isFromEdit: Boolean,
            transaction_type: String

        ) {
            itemView.apply {

                tvItemNameTaxAnalysis.setText(itemViewModel.item_name)
                 tvLedgerNameTaxAnalysis.setText(itemViewModel.ledger_name)
                 tvTaxableAmtTaxAnalysis.setText(itemViewModel.taxable_amount)
                 tvGSTRateTaxAnalysis.setText(itemViewModel.gst_rate_percentage)
                tvHsnSacTaxAnalysis.setText(itemViewModel.hsn)
                tvigstAmtTaxAnalysis.setText(itemViewModel.igst_amount)
                tvsgstAmtTaxAnalysis.setText(itemViewModel.sgst_amount)
                tvcgstAmtTaxAnalysis.setText(itemViewModel.cgst_amount)

                cardSalesItemRowTaxAnalysis.clickWithDebounce {
                    cardSalesItemRowTaxAnalysis.context.startActivity(
                        Intent(
                            cardSalesItemRowTaxAnalysis.context,
                            AddTaxAnalysisActivity::class.java
                        ).putExtra(
                            Constants.TAX_ANALYSIS_MODEL,
                            Gson().toJson(itemViewModel)
                        ).putExtra(
                            Constants.TRANSACTION_TYPE,
                            transaction_type
                        )

                    )

                }


            }

        }

    }
}



