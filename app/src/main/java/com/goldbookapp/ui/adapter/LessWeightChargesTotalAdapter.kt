package com.goldbookapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.CalculationPaymentModel
import com.goldbookapp.utils.Constants
import kotlinx.android.synthetic.main.row_charges_studded_lesswtchrages.view.*

class LessWeightChargesTotalAdapter(
    private val mList: ArrayList<CalculationPaymentModel.DataPayment.ItemPayment.LessWeights>,
    private val isFromEdit: Boolean
) :
    RecyclerView.Adapter<LessWeightChargesTotalAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_charges_studded_lesswtchrages, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val ItemsViewModel = mList[position]


        holder.bind(mList[position], position, isFromEdit)

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        fun bind(
            itemViewModel: CalculationPaymentModel.DataPayment.ItemPayment.LessWeights,
            position: Int,
            isFromEdit: Boolean

        ) {
            itemView.apply {
                val lesswtStringBuilder: StringBuilder = StringBuilder()
                lesswtStringBuilder.append(Constants.AMOUNT_RS_APPEND)
                    .append(itemViewModel.amount)
                    .append(" of ")
                    .append(itemViewModel.name)
                itemView.txtRowChargesLesswtValue.setText(lesswtStringBuilder)
                }

        }

    }
}



