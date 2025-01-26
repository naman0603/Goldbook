package com.goldbookapp.ui.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.color
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.SalesLineModel
import com.goldbookapp.ui.activity.payment.NewPaymentActivity
import com.goldbookapp.ui.activity.purchase.NewPurchaseActivity
import com.goldbookapp.ui.activity.receipt.NewReceiptActivity
import com.goldbookapp.ui.activity.sales.NewInvoiceActivity
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import kotlinx.android.synthetic.main.row_cashbank_subtrans.view.*


class IssueReceiveAdapter(
    private val issueReceivelist: ArrayList<SalesLineModel.SaleLineModelDetails>,
    private val isFromModule: String,
    private val isFromDetail: Boolean,
    private val debit_short_term: String,
    private val credit_short_term: String
) : RecyclerView.Adapter<IssueReceiveAdapter.DataViewHolder>() {

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var prefs: SharedPreferences

        fun bind(
            issueReceive_rowModel: SalesLineModel.SaleLineModelDetails,
            position: Int,
            isFromModule: String,
            isFromDetail: Boolean,
            debit_short_term: String,
            credit_short_term: String
        ) {

            itemView.apply {

                if (!isFromDetail) {
                    llRowCashbankRoot.clickWithDebounce {

                        InvoiceItemEditDeleteDialog(
                            llRowCashbankRoot.context,
                            issueReceive_rowModel,
                            position,
                            isFromModule
                        )
                    }
                }

                when (issueReceive_rowModel.type) {
                    "cash_receipt" -> {
                        itemView.row_cbr_subtrans_firstcolumn.setText("Cash Receipt")
                        itemView.row_cbr_subtrans_secondcolumn.setText("-")

                        val thirdColumnText = SpannableStringBuilder()
                            .append(issueReceive_rowModel.cash_amount + " ")
                            .color(
                                ContextCompat.getColor(context, R.color.credit_color),
                                { append(credit_short_term) })
                        itemView.row_cbr_subtrans_thirdcolumn.setText(thirdColumnText)


                    }
                    "cash_payment" -> {
                        itemView.row_cbr_subtrans_firstcolumn.setText("Cash Payment")
                        itemView.row_cbr_subtrans_secondcolumn.setText("-")

                        val thirdColumnText = SpannableStringBuilder()
                            .append(issueReceive_rowModel.cash_amount + " ")
                            .color(
                                ContextCompat.getColor(context, R.color.debit_color),
                                { append(debit_short_term) })
                        itemView.row_cbr_subtrans_thirdcolumn.setText(thirdColumnText)


                    }
                    "bank_receipt" -> {
                        itemView.row_cbr_subtrans_firstcolumn.setText("Bank Receipt")
                        itemView.row_cbr_subtrans_secondcolumn.setText("-")

                        val thirdColumnText = SpannableStringBuilder()
                            .append(issueReceive_rowModel.bank_final_amt + " ")
                            .color(
                                ContextCompat.getColor(context, R.color.credit_color),
                                { append(credit_short_term) })
                        itemView.row_cbr_subtrans_thirdcolumn.setText(thirdColumnText)


                    }
                    "bank_payment" -> {
                        itemView.row_cbr_subtrans_firstcolumn.setText("Bank Payment")
                        itemView.row_cbr_subtrans_secondcolumn.setText("-")

                        val thirdColumnText = SpannableStringBuilder()
                            .append(issueReceive_rowModel.bank_final_amt + " ")
                            .color(
                                ContextCompat.getColor(context, R.color.debit_color),
                                { append(debit_short_term) })
                        itemView.row_cbr_subtrans_thirdcolumn.setText(thirdColumnText)


                    }
                    "metal_receipt" -> {

                        itemView.row_cbr_subtrans_firstcolumn.setText("MR:+${issueReceive_rowModel.item_name}")

                        when(issueReceive_rowModel.maintain_stock_in_name_metal){
                            "Kilograms"->{
                                val secondColumnText = SpannableStringBuilder()
                                    .append(CommonUtils.kgTogms(issueReceive_rowModel.fine_wt.toString()) + " ")
                                    .color(
                                        ContextCompat.getColor(context, R.color.credit_color),
                                        { append(credit_short_term) })
                                itemView.row_cbr_subtrans_secondcolumn.setText(secondColumnText)
                            }
                            "Grams"->{
                                val secondColumnText = SpannableStringBuilder()
                                    .append(issueReceive_rowModel.fine_wt.toString() + " ")
                                    .color(
                                        ContextCompat.getColor(context, R.color.credit_color),
                                        { append(credit_short_term) })
                                itemView.row_cbr_subtrans_secondcolumn.setText(secondColumnText)
                            }
                            "Carat"->{
                                val secondColumnText = SpannableStringBuilder()
                                    .append(CommonUtils.carrotTogm(issueReceive_rowModel.fine_wt.toString()) + " ")
                                    .color(
                                        ContextCompat.getColor(context, R.color.credit_color),
                                        { append(credit_short_term) })
                                itemView.row_cbr_subtrans_secondcolumn.setText(secondColumnText)
                            }
                        }

                        itemView.row_cbr_subtrans_thirdcolumn.setText("-")


                    }
                    "metal_payment" -> {

                        itemView.row_cbr_subtrans_firstcolumn.setText("MP:+${issueReceive_rowModel.item_name}")
                        when(issueReceive_rowModel.maintain_stock_in_name_metal){
                            "Kilograms"->{
                                val secondColumnText = SpannableStringBuilder()
                                    .append(CommonUtils.kgTogms(issueReceive_rowModel.fine_wt.toString()) + " ")
                                    .color(
                                        ContextCompat.getColor(context, R.color.debit_color),
                                        { append(debit_short_term) })
                                itemView.row_cbr_subtrans_secondcolumn.setText(secondColumnText)

                            }
                            "Grams"->{
                                val secondColumnText = SpannableStringBuilder()
                                    .append(issueReceive_rowModel.fine_wt + " ")
                                    .color(
                                        ContextCompat.getColor(context, R.color.debit_color),
                                        { append(debit_short_term) })
                                itemView.row_cbr_subtrans_secondcolumn.setText(secondColumnText)

                            }
                            "Carat"->{
                                val secondColumnText = SpannableStringBuilder()
                                    .append(CommonUtils.carrotTogm(issueReceive_rowModel.fine_wt.toString()) + " ")
                                    .color(
                                        ContextCompat.getColor(context, R.color.debit_color),
                                        { append(debit_short_term) })
                                itemView.row_cbr_subtrans_secondcolumn.setText(secondColumnText)

                            }
                        }

                        itemView.row_cbr_subtrans_thirdcolumn.setText("-")


                    }
                    "rate_cut" -> {
                        /*when (issueReceive_rowModel.rate_cut_type) {
                            "fine" -> {*/
                        val metal_rate_cut = issueReceive_rowModel.metal_type_id_rate_cut
                        if(metal_rate_cut.equals("1")){
                            itemView.row_cbr_subtrans_firstcolumn.setText("G: RC @${issueReceive_rowModel.rcm_gold_rate}")
                        }else{
                            itemView.row_cbr_subtrans_firstcolumn.setText("S: RC @${issueReceive_rowModel.rcm_gold_rate}")
                        }


                        when(issueReceive_rowModel.rate_cut_fine_term){
                            "credit"->{
                                val secondColumnText = SpannableStringBuilder()
                                    .append(issueReceive_rowModel.rate_cut_fine + " ")
                                    .color(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.credit_color
                                        ),
                                        { append(credit_short_term) })
                                itemView.row_cbr_subtrans_secondcolumn.setText(
                                    secondColumnText
                                )

                                val thirdColumnText = SpannableStringBuilder()
                                    .append(issueReceive_rowModel.rate_cut_amount + " ")
                                    .color(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.debit_color
                                        ),
                                        { append(debit_short_term) })
                                itemView.row_cbr_subtrans_thirdcolumn.setText(
                                    thirdColumnText
                                )
                            }
                            "debit"->{
                                val secondColumnText = SpannableStringBuilder()
                                    .append(issueReceive_rowModel.rate_cut_fine + " ")
                                    .color(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.debit_color
                                        ),
                                        { append(debit_short_term) })
                                itemView.row_cbr_subtrans_secondcolumn.setText(
                                    secondColumnText
                                )

                                val thirdColumnText = SpannableStringBuilder()
                                    .append(issueReceive_rowModel.rate_cut_amount + " ")
                                    .color(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.credit_color
                                        ),
                                        { append(credit_short_term) })
                                itemView.row_cbr_subtrans_thirdcolumn.setText(
                                    thirdColumnText
                                )
                            }
                        }



                    }
                    // }
                    /* "amount" -> {
                         itemView.row_cbr_subtrans_firstcolumn.setText("Rate-Cut @${issueReceive_rowModel.rate_cut_fine}")
                         when (isFromModule) {
                             "sales" -> {
                                 val secondColumnText = SpannableStringBuilder()
                                     .append(issueReceive_rowModel.rate_cut_fine + " ")
                                     .color(
                                         ContextCompat.getColor(
                                             context,
                                             R.color.credit_color
                                         ),
                                         { append(credit_short_term) })
                                 itemView.row_cbr_subtrans_secondcolumn.setText(
                                     secondColumnText
                                 )

                                 val thirdColumnText = SpannableStringBuilder()
                                     .append(issueReceive_rowModel.rate_cut_amount + " ")
                                     .color(
                                         ContextCompat.getColor(
                                             context,
                                             R.color.debit_color
                                         ),
                                         { append(debit_short_term) })
                                 itemView.row_cbr_subtrans_thirdcolumn.setText(
                                     thirdColumnText
                                 )
                             }
                             "purchase" -> {
                                 val secondColumnText = SpannableStringBuilder()
                                     .color(
                                         ContextCompat.getColor(
                                             context,
                                             R.color.debit_color
                                         ),
                                         { append(debit_short_term) })
                                     .append(" " + issueReceive_rowModel.rate_cut_fine)
                                 itemView.row_cbr_subtrans_secondcolumn.setText(
                                     secondColumnText
                                 )

                                 val thirdcolumnText = SpannableStringBuilder()
                                     .color(
                                         ContextCompat.getColor(
                                             context,
                                             R.color.credit_color
                                         ),
                                         { append(credit_short_term) })
                                     .append(" " + issueReceive_rowModel.rate_cut_amount)
                                 itemView.row_cbr_subtrans_secondcolumn.setText(
                                     thirdcolumnText
                                 )
                             }
                             else -> {
                                 itemView.row_cbr_subtrans_secondcolumn.setText(
                                     issueReceive_rowModel.rate_cut_fine
                                 )
                                 itemView.row_cbr_subtrans_thirdcolumn.setText("-" + issueReceive_rowModel.rate_cut_amount)
                             }

                         }
                         *//*itemView.row_cbr_subtrans_secondcolumn.setText(issueReceive_rowModel.rate_cut_fine)
                                itemView.row_cbr_subtrans_thirdcolumn.setText("-" + issueReceive_rowModel.rate_cut_amount)*//*
                            }*/
                    //  }

                    "adjustment" -> {
                        when(isFromModule){
                            "payment"->{
                                when (issueReceive_rowModel.adjustment_fine) {
                                    "0.000" -> {
                                        itemView.row_cbr_subtrans_firstcolumn.setText("Adjustment")
                                        itemView.row_cbr_subtrans_secondcolumn.setText("")

                                        val thirdColumnText = SpannableStringBuilder()
                                            .append(issueReceive_rowModel.adjustment_amount + " ")
                                            .color(
                                                ContextCompat.getColor(
                                                    context,
                                                    R.color.debit_color
                                                ),
                                                { append(debit_short_term) })
                                        itemView.row_cbr_subtrans_thirdcolumn.setText(thirdColumnText)
                                    }
                                    ""->{
                                        itemView.row_cbr_subtrans_firstcolumn.setText("Adjustment")
                                        itemView.row_cbr_subtrans_secondcolumn.setText("")

                                        val thirdColumnText = SpannableStringBuilder()
                                            .append(issueReceive_rowModel.adjustment_amount + " ")
                                            .color(
                                                ContextCompat.getColor(
                                                    context,
                                                    R.color.debit_color
                                                ),
                                                { append(debit_short_term) })
                                        itemView.row_cbr_subtrans_thirdcolumn.setText(thirdColumnText)
                                    }
                                    else -> {
                                        itemView.row_cbr_subtrans_firstcolumn.setText("Adjustment")
                                        val secondColumnText = SpannableStringBuilder()
                                            .append(issueReceive_rowModel.adjustment_fine + " ")
                                            .color(
                                                ContextCompat.getColor(
                                                    context,
                                                    R.color.debit_color
                                                ),
                                                { append(debit_short_term) })
                                        itemView.row_cbr_subtrans_secondcolumn.setText(secondColumnText)


                                        val thirdColumnText = SpannableStringBuilder()
                                            .append(issueReceive_rowModel.adjustment_amount + " ")
                                            .color(
                                                ContextCompat.getColor(
                                                    context,
                                                    R.color.debit_color
                                                ),
                                                { append(debit_short_term) })

                                        itemView.row_cbr_subtrans_thirdcolumn.setText(thirdColumnText)
                                    }
                                }
                            }
                            "receipt"->{
                                when (issueReceive_rowModel.adjustment_fine) {
                                    "0.000" -> {
                                        itemView.row_cbr_subtrans_firstcolumn.setText("Adjustment")
                                        itemView.row_cbr_subtrans_secondcolumn.setText("")

                                        val thirdColumnText = SpannableStringBuilder()
                                            .append(issueReceive_rowModel.adjustment_amount + " ")
                                            .color(
                                                ContextCompat.getColor(
                                                    context,
                                                    R.color.credit_color
                                                ),
                                                { append(credit_short_term) })
                                        itemView.row_cbr_subtrans_thirdcolumn.setText(thirdColumnText)
                                    }
                                    "" -> {
                                        itemView.row_cbr_subtrans_firstcolumn.setText("Adjustment")
                                        itemView.row_cbr_subtrans_secondcolumn.setText("")

                                        val thirdColumnText = SpannableStringBuilder()
                                            .append(issueReceive_rowModel.adjustment_amount + " ")
                                            .color(
                                                ContextCompat.getColor(
                                                    context,
                                                    R.color.credit_color
                                                ),
                                                { append(credit_short_term) })
                                        itemView.row_cbr_subtrans_thirdcolumn.setText(thirdColumnText)
                                    }
                                    else -> {
                                        itemView.row_cbr_subtrans_firstcolumn.setText("Adjustment")
                                        val secondColumnText = SpannableStringBuilder()
                                            .append(issueReceive_rowModel.adjustment_fine + " ")
                                            .color(
                                                ContextCompat.getColor(
                                                    context,
                                                    R.color.credit_color
                                                ),
                                                { append(credit_short_term) })
                                        itemView.row_cbr_subtrans_secondcolumn.setText(secondColumnText)


                                        val thirdColumnText = SpannableStringBuilder()
                                            .append(issueReceive_rowModel.adjustment_amount + " ")
                                            .color(
                                                ContextCompat.getColor(
                                                    context,
                                                    R.color.credit_color
                                                ),
                                                { append(credit_short_term) })

                                        itemView.row_cbr_subtrans_thirdcolumn.setText(thirdColumnText)
                                    }
                                }
                            }
                        }


                    }
                }

            }
        }

        fun InvoiceItemEditDeleteDialog(
            context: Context,
            issueReceive_rowModel: SalesLineModel.SaleLineModelDetails,
            position: Int,
            isFromModule: String
        ) {

            val builder = AlertDialog.Builder(context)

            val itemEditAddClick = { dialog: DialogInterface, which: Int ->
                when (isFromModule) {
                    "sales" -> {
                        (context as NewInvoiceActivity).editIssueReceiveItem(
                            position,
                            issueReceive_rowModel
                        )
                    }
                    "purchase" -> {
                        (context as NewPurchaseActivity).editIssueReceiveItem(
                            position,
                            issueReceive_rowModel
                        )
                    }
                    "receipt" -> {
                        (context as NewReceiptActivity).editIssueReceiveItem(
                            position,
                            issueReceive_rowModel
                        )
                    }
                    "payment" -> {
                        (context as NewPaymentActivity).editIssueReceiveItem(
                            position,
                            issueReceive_rowModel
                        )
                    }
                }

            }
            val itemRemoveClick = { dialog: DialogInterface, which: Int ->
                when (isFromModule) {
                    "sales" -> {
                        (context as NewInvoiceActivity).removeIssueReceiveItem(position)
                    }
                    "purchase" -> {
                        (context as NewPurchaseActivity).removeIssueReceiveItem(position)
                    }
                    "receipt" -> {
                        (context as NewReceiptActivity).removeIssueReceiveItem(position)
                    }
                    "payment" -> {
                        (context as NewPaymentActivity).removeIssueReceiveItem(position)
                    }
                }

            }
            with(builder)
            {
                setTitle(context.getString(R.string.editDeleteIssueResDialogTitle))
                setMessage(context.getString(R.string.editDeleteIssueResDialogMessage))
                setPositiveButton(
                    context.getString(R.string.editDeleteIssueRecDialogPosbtn),
                    DialogInterface.OnClickListener(function = itemEditAddClick)
                )
                val neutralButton = setNeutralButton(
                    context.getString(R.string.editDeleteIssueRecDialogNeutralbtn),
                    itemRemoveClick
                )
                show()
            }


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_cashbank_subtrans, parent, false)
        )

    override fun getItemCount(): Int = issueReceivelist.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(
            issueReceivelist[position], position, isFromModule, isFromDetail,
            debit_short_term, credit_short_term
        )
    }

    fun addissueReceiveList(issueReceivelist: ArrayList<SalesLineModel.SaleLineModelDetails>) {
        this.issueReceivelist.apply {
            clear()
            if (issueReceivelist != null) {
                addAll(issueReceivelist)
            }
        }

    }
}
