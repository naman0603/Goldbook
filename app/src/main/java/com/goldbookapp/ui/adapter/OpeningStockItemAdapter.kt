package com.goldbookapp.ui.adapter


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
import com.goldbookapp.ui.activity.openingstock.NewOpeningStockActivity
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_item.view.*
import java.math.BigDecimal
import java.math.RoundingMode

class OpeningStockItemAdapter(
    private val newpurchaseitemlist: ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>,
    private val isfromNewOpeningStock: Boolean
) : RecyclerView.Adapter<OpeningStockItemAdapter.DataViewHolder>() {

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var prefs: SharedPreferences

        fun bind(
            openingStock_item_rowModel: OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem,
            position: Int,
            isfromNewOpeningStock: Boolean
        ) {

            itemView.apply {
                iv_salebillrowitem_deleteitem.visibility = View.GONE

                linear_row_item.clickWithDebounce {

                    when (isfromNewOpeningStock) {
                        true -> {
                            InvoiceItemEditDeleteDialog(
                                linear_row_item.context,
                                isfromNewOpeningStock,
                                openingStock_item_rowModel
                            )
                        }
                        else->{

                        }
                    }
                }

                tv_salebillrowitem_left_item_name.text =
                    openingStock_item_rowModel.item_name.toString()
                when (openingStock_item_rowModel.item_quantity) {
                    "0.00" -> {
                        tv_salebillrowitem_right_itemquantity.visibility = View.GONE
                    }
                    "0" -> {
                        tv_salebillrowitem_right_itemquantity.visibility = View.GONE
                    }
                    else -> {
                        tv_salebillrowitem_right_itemquantity.visibility = View.VISIBLE
                        tv_salebillrowitem_right_itemquantity.text =
                            openingStock_item_rowModel.item_quantity.toString() + " " + openingStock_item_rowModel.item_unit_name.toString()
                    }
                }

                tv_salebillrowitem_left_tagNo.visibility = View.GONE
                tv_salebillrowitem_right_fine_wt.text =
                    openingStock_item_rowModel.item_fine_wt.toString() + " (F)"

                val wastage: Float = openingStock_item_rowModel.item_wastage.toFloat()
                val touch: Float = openingStock_item_rowModel.item_touch.toFloat()

                val totalOfTouchWast = (touch.plus(wastage))


                tv_salebillrowitem_left_itemcalculation.text =
                    openingStock_item_rowModel.item_gross_wt.toString() +
                            " - " + ("%.3f".format(openingStock_item_rowModel.item_less_wt?.toDouble())) + "\n" +
                            "= " + openingStock_item_rowModel.item_net_wt + " x " + totalOfTouchWast

                when (openingStock_item_rowModel.item_rate.equals("0.00")) {
                    true -> {
                        ly_salebillrowitem_gold.visibility = View.GONE
                    }
                    false -> {
                        ly_salebillrowitem_gold.visibility = View.VISIBLE
                    }
                }

                tv_salebillrowitem_left_goldrate.text = "@ " + openingStock_item_rowModel.item_rate

                val goldRate: BigDecimal = openingStock_item_rowModel.item_rate.toBigDecimal()
                val fineWeight: BigDecimal = openingStock_item_rowModel.item_fine_wt.toBigDecimal()
                val totalAmt: String =
                    ((goldRate.setScale(3)
                        .multiply(fineWeight.setScale(3, RoundingMode.CEILING))
                            )).setScale(2, RoundingMode.CEILING).toString()

                tv_salebillrowitem_right_gold_amount.text = totalAmt

                tv_salebillrowitem_right_amountone.text =
                    "₹" + openingStock_item_rowModel.item_total


            }
        }

        fun InvoiceItemEditDeleteDialog(
            context: Context,
            isfromNewOpeningStock: Boolean,
            openingstockItemRowmodel: OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem
        ) {

            val builder = AlertDialog.Builder(context)
            val itemEditAddClick = { diaOpeningSlog: DialogInterface, which: Int ->

                context.startActivity(
                    Intent(context, AddItemActivity::class.java)
                        .putExtra(
                            Constants.TRANSACTION_TYPE,
                            "opening_stock"
                        )
                        .putExtra(Constants.OPENING_STOCK_POSITION_KEY, position)
                        .putExtra(Constants.EDIT_ITEM, Constants.EDIT_ITEM)
                        .putExtra(Constants.OPENING_STOCK_SAVE_TYPE, 2)
                        .putExtra(
                            Constants.OPENING_STOCK_DETAIL_KEY,
                            Gson().toJson(openingstockItemRowmodel)
                        )

                )
            }
            val itemRemoveClick = { dialog: DialogInterface, which: Int ->
                DeleteConfirmDialog(context, isfromNewOpeningStock)
            }
            with(builder)
            {
                setTitle(context.getString(R.string.editDeleteOpeningStockDialogTitle))
                setMessage(context.getString(R.string.editDeleteItemDialogMessage))
                setPositiveButton(
                    context.getString(R.string.editDeleteItemDialogPosbtn),
                    DialogInterface.OnClickListener(function = itemEditAddClick)
                )
                val neutralButton = setNeutralButton(
                    context.getString(R.string.editDeleteItemDialogNeutralbtn),
                    itemRemoveClick
                )
                show()
            }


        }

        fun DeleteConfirmDialog(context: Context, isfromNewOpeningStock: Boolean) {

            val builder = AlertDialog.Builder(context)
            val itemDeleteClick = { dialog: DialogInterface, which: Int ->
                if (isfromNewOpeningStock)
                    (context as NewOpeningStockActivity).removeItem(position)
                else
                    (context as NewOpeningStockActivity).removeItem(position)


            }
            val dialogdismiss = { dialog: DialogInterface, which: Int ->
                dialog.dismiss()
            }
            with(builder)
            {
                setTitle(context.getString(R.string.DelDialogTitle))
                setMessage(context.getString(R.string.DelDialogMessage))
                setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
                setNeutralButton(
                    context.getString(R.string.Delete),
                    DialogInterface.OnClickListener(function = itemDeleteClick)
                )
                show()
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.row_item, parent, false)
        )

    override fun getItemCount(): Int = newpurchaseitemlist.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(newpurchaseitemlist[position], position, isfromNewOpeningStock)
    }

    fun addpurchasebillrow_item(newpurchaserowitemlist: List<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>?) {
        this.newpurchaseitemlist.apply {
            clear()
            if (newpurchaserowitemlist != null) {
                addAll(newpurchaserowitemlist)
            }
        }

    }
}

