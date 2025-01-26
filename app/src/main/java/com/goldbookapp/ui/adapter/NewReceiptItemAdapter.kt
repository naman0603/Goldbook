package com.goldbookapp.ui.adapter


//import com.goldbookapp.model.CalculationReceiptModel
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.OpeningStockItemCalculationModel
import com.goldbookapp.ui.activity.additem.AddItemActivity
import com.goldbookapp.ui.activity.receipt.NewReceiptActivity
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_item.view.*
import java.math.BigDecimal
import java.math.RoundingMode


class NewReceiptItemAdapter(
    private val newReceiptitemlist: ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>
   ) : RecyclerView.Adapter<NewReceiptItemAdapter.DataViewHolder>(){

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var prefs: SharedPreferences

        fun bind(
            addreceipt_item_rowModel: OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem,
            position: Int

        ) {

            itemView.apply {

                iv_salebillrowitem_deleteitem.visibility = View.GONE
                linear_row_item.clickWithDebounce {

                    ReceiptItemEditDeleteDialog(
                        linear_row_item.context,
                        addreceipt_item_rowModel
                    )
                }
                tv_salebillrowitem_left_item_name.text =
                    addreceipt_item_rowModel.item_name.toString()
                when (addreceipt_item_rowModel.item_quantity) {
                    "0.00" -> {
                        tv_salebillrowitem_right_itemquantity.visibility = View.GONE
                    }
                    "0"->{
                        tv_salebillrowitem_right_itemquantity.visibility = View.GONE
                    }
                    else -> {
                        tv_salebillrowitem_right_itemquantity.visibility = View.VISIBLE
                        tv_salebillrowitem_right_itemquantity.text =
                            addreceipt_item_rowModel.item_quantity.toString() + " " + addreceipt_item_rowModel.item_unit_name.toString()
                    }
                }

                when(addreceipt_item_rowModel.tag_no.equals("")){
                    true->{
                        tv_salebillrowitem_left_tagNo.visibility = View.GONE
                    }
                    false->{
                        tv_salebillrowitem_left_tagNo.visibility = View.VISIBLE
                    }
                }

                tv_salebillrowitem_left_tagNo.text = "Tag# "+ addreceipt_item_rowModel.tag_no

                tv_salebillrowitem_right_fine_wt.text =
                    addreceipt_item_rowModel.item_fine_wt.toString() + " (F)"

                tv_salebillrowitem_right_amountone.text= addreceipt_item_rowModel.item_total

                when (addreceipt_item_rowModel.item_type.equals("Goods")) {
                    true->{
                        ly_salebillrowitem_calculation.visibility = View.VISIBLE
                    }
                    false->{
                        ly_salebillrowitem_calculation.visibility = View.GONE
                    }
                }

                val wastage: Float = addreceipt_item_rowModel.item_wastage.toFloat()
                val touch: Float = addreceipt_item_rowModel.item_touch.toFloat()

                val totalOfTouchWast = (touch.plus(wastage))

                tv_salebillrowitem_left_itemcalculation.text =
                    addreceipt_item_rowModel.item_gross_wt.toString() +
                            " - " + addreceipt_item_rowModel.item_less_wt + "\n" +
                            "= " + addreceipt_item_rowModel.item_net_wt + " x " + totalOfTouchWast

                     when(addreceipt_item_rowModel.item_rate.equals("0.00")){
                            true->{
                                ly_salebillrowitem_gold.visibility = View.GONE
                            }
                            false->{
                                ly_salebillrowitem_gold.visibility = View.VISIBLE
                    }
                }

                tv_salebillrowitem_left_goldrate.text = "@ "+addreceipt_item_rowModel.item_rate

                when (addreceipt_item_rowModel.item_type.equals("Goods")) {
                    true -> {

                        when(addreceipt_item_rowModel.item_rate_on){
                            "fine"->{
                                val goldRate: BigDecimal = addreceipt_item_rowModel.item_rate.toBigDecimal()
                                val fineWeight: BigDecimal = addreceipt_item_rowModel.item_fine_wt.toBigDecimal()
                                val totalAmt: String =
                                    ((goldRate.setScale(3)
                                        .multiply(fineWeight.setScale(3, RoundingMode.CEILING))
                                            )).setScale(2, RoundingMode.CEILING).toString()

                                tv_salebillrowitem_right_gold_amount.text = totalAmt
                            }
                            "net"->{
                                val goldRate: BigDecimal = addreceipt_item_rowModel.item_rate.toBigDecimal()
                                val netWeight: BigDecimal = addreceipt_item_rowModel.item_net_wt.toBigDecimal()
                                val totalAmt: String =
                                    ((goldRate.setScale(3)
                                        .multiply(netWeight.setScale(3, RoundingMode.CEILING))
                                            )).setScale(2, RoundingMode.CEILING).toString()

                                tv_salebillrowitem_right_gold_amount.text = totalAmt
                            }
                            "gross"->{
                                val goldRate: BigDecimal = addreceipt_item_rowModel.item_rate.toBigDecimal()
                                val grossWeight: BigDecimal = addreceipt_item_rowModel.item_gross_wt.toBigDecimal()
                                val totalAmt: String =
                                    ((goldRate.setScale(3)
                                        .multiply(grossWeight.setScale(3, RoundingMode.CEILING))
                                            )).setScale(2, RoundingMode.CEILING).toString()

                                tv_salebillrowitem_right_gold_amount.text = totalAmt
                            }
                            "fix"->{
                                val goldRate: BigDecimal = addreceipt_item_rowModel.item_rate.toBigDecimal()
                                val totalAmt :String = goldRate.toString()
                                tv_salebillrowitem_right_gold_amount.text = totalAmt
                            }
                        }

                    }
                    false -> {
                        val goldRate: BigDecimal = addreceipt_item_rowModel.item_rate.toBigDecimal()
                        val unit: BigDecimal = addreceipt_item_rowModel.item_quantity.toBigDecimal()
                        val totalAmt: String =
                            ((goldRate.setScale(3)
                                .multiply(unit.setScale(3, RoundingMode.CEILING))
                                    )).setScale(2, RoundingMode.CEILING).toString()
                        tv_salebillrowitem_right_gold_amount.text = totalAmt
                    }
                }

                when (addreceipt_item_rowModel.item_remarks.isNullOrBlank()) {
                    true -> {
                        tv_salebillrowitem_left_remarks.visibility = View.GONE
                    }
                    false -> {
                        tv_salebillrowitem_left_remarks.visibility = View.VISIBLE
                        tv_salebillrowitem_left_remarks.setText("Note: " + addreceipt_item_rowModel.item_remarks)
                    }
                }

            }

        }


        fun ReceiptItemEditDeleteDialog(context: Context,
                                        addreceipt_item_rowModel: OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem) {

            val builder = AlertDialog.Builder(context)
            val itemEditAddClick = { dialog: DialogInterface, which: Int ->
                context.startActivity(
                    Intent(context, AddItemActivity::class.java)
                    .putExtra(
                        Constants.TRANSACTION_TYPE,
                        "receipt"
                    )
                    .putExtra(Constants.OPENING_STOCK_POSITION_KEY, position)
                    .putExtra(Constants.EDIT_ITEM, Constants.EDIT_ITEM)
                    .putExtra(Constants.OPENING_STOCK_SAVE_TYPE,2)
                    .putExtra(Constants.OPENING_STOCK_DETAIL_KEY, Gson().toJson(addreceipt_item_rowModel)))


            }

            val itemRemoveClick = { dialog: DialogInterface, which: Int ->
                DeleteConfirmDialog(context)
            }
            with(builder)
            {
                setTitle(context.getString(R.string.editDeleteItemReceiptDialogTitle))
                setMessage(context.getString(R.string.editDeleteItemDialogMessage))
                setPositiveButton(context.getString(R.string.editDeleteItemDialogPosbtn), DialogInterface.OnClickListener(function = itemEditAddClick))
                val neutralButton = setNeutralButton(context.getString(R.string.editDeleteItemDialogNeutralbtn), itemRemoveClick)
                show()
            }


        }
        fun DeleteConfirmDialog(context: Context) {

            val builder = AlertDialog.Builder(context)
            val itemDeleteClick = { dialog: DialogInterface, which: Int ->
                (context as NewReceiptActivity).removeItem(position)

            }
            val dialogdismiss = { dialog: DialogInterface, which: Int ->
                dialog.dismiss()
            }
            with(builder)
            {
                setTitle(context.getString(R.string.DelDialogTitle))
                setMessage(context.getString(R.string.DelDialogMessage))
                setPositiveButton(context.getString(R.string.Cancel),dialogdismiss)
                setNeutralButton(context.getString(R.string.Delete), DialogInterface.OnClickListener(function = itemDeleteClick))
                show()
            }


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_item, parent, false))

    override fun getItemCount(): Int = newReceiptitemlist.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(newReceiptitemlist[position], position)
    }

    fun addReceiptItems(newReceiptRowitemlist: List<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>?) {
        this.newReceiptitemlist.apply {
            clear()
            if (newReceiptRowitemlist != null) {
                addAll(newReceiptRowitemlist)
            }
        }

    }
}





