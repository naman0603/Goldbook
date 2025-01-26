package com.goldbookapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.CalculateSalesModel
import com.goldbookapp.model.OpeningStockItemCalculationModel
import com.goldbookapp.model.OpeningStockItemModel
import com.goldbookapp.model.SaleDetailModel
import kotlinx.android.synthetic.main.row_item.view.*
import java.math.BigDecimal
import java.math.RoundingMode

class SaleDetailBill_ItemAdapter(private val saledetailitemlist: ArrayList<SaleDetailModel.Item1427117511>) :
    RecyclerView.Adapter<SaleDetailBill_ItemAdapter.DataViewHolder>() {

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(salesbill_item_rowModel: SaleDetailModel.Item1427117511) {
            itemView.apply {

                iv_salebillrowitem_deleteitem.visibility = View.GONE
                tv_salebillrowitem_left_item_name.text =
                    salesbill_item_rowModel.item_name.toString()
                //tv_salebillrowitem_right_itemquantity.text=salesbill_item_rowModel.quantity.toString() + " pcs"
                when (salesbill_item_rowModel.item_quantity) {
                    "0.00" -> {
                        tv_salebillrowitem_right_itemquantity.visibility = View.GONE
                    }
                    "0"->{
                        tv_salebillrowitem_right_itemquantity.visibility = View.GONE
                    }
                    else -> {
                        tv_salebillrowitem_right_itemquantity.visibility = View.VISIBLE
                        tv_salebillrowitem_right_itemquantity.text =
                            salesbill_item_rowModel.item_quantity.toString() + " " + salesbill_item_rowModel.item_unit_name.toString()
                    }
                }
                // tv_salebillrowitem_right_itemquantity.text=salesbill_item_rowModel.quantity.toString() + " " + salesbill_item_rowModel.unit_name.toString()

                when(salesbill_item_rowModel.tag_no.equals("")){
                    true->{
                        tv_salebillrowitem_left_tagNo.visibility = View.GONE
                    }
                    false->{
                        tv_salebillrowitem_left_tagNo.visibility = View.VISIBLE
                    }
                }

                tv_salebillrowitem_left_tagNo.text = "Tag# "+ salesbill_item_rowModel.tag_no

                tv_salebillrowitem_right_fine_wt.text =
                    salesbill_item_rowModel.item_fine_wt.toString() + " (F)"

                tv_salebillrowitem_right_amountone.text = salesbill_item_rowModel.item_total

                val wastage: Float = salesbill_item_rowModel.item_wastage.toFloat()
                val touch: Float = salesbill_item_rowModel.item_touch.toFloat()

                val totalOfTouchWast = (touch.plus(wastage))

                when (salesbill_item_rowModel.item_type.equals("Goods")) {
                    true->{
                        ly_salebillrowitem_calculation.visibility = View.VISIBLE
                    }
                    false->{
                        ly_salebillrowitem_calculation.visibility = View.GONE
                    }
                }
                tv_salebillrowitem_left_itemcalculation.text =
                    salesbill_item_rowModel.item_gross_wt.toString() +
                            " - " + salesbill_item_rowModel.item_less_wt + "\n" +
                            "= " + salesbill_item_rowModel.item_net_wt + " x " + totalOfTouchWast

                when(salesbill_item_rowModel.item_rate.equals("0.00")){
                    true->{
                        ly_salebillrowitem_gold.visibility = View.GONE
                    }
                    false->{
                        ly_salebillrowitem_gold.visibility = View.VISIBLE
                    }
                }



                tv_salebillrowitem_left_goldrate.text = "@ "+salesbill_item_rowModel.item_rate

                when (salesbill_item_rowModel.item_type.equals("Goods")) {
                    true -> {
                        when(salesbill_item_rowModel.item_rate_on){
                            "fine"->{
                                val goldRate: BigDecimal = salesbill_item_rowModel.item_rate.toBigDecimal()
                                val fineWeight: BigDecimal = salesbill_item_rowModel.item_fine_wt.toBigDecimal()
                                val totalAmt: String =
                                    ((goldRate.setScale(3)
                                        .multiply(fineWeight.setScale(3, RoundingMode.CEILING))
                                            )).setScale(2, RoundingMode.CEILING).toString()

                                tv_salebillrowitem_right_gold_amount.text = totalAmt
                            }
                            "net"->{
                                val goldRate: BigDecimal = salesbill_item_rowModel.item_rate.toBigDecimal()
                                val netWeight: BigDecimal = salesbill_item_rowModel.item_net_wt.toBigDecimal()
                                val totalAmt: String =
                                    ((goldRate.setScale(3)
                                        .multiply(netWeight.setScale(3, RoundingMode.CEILING))
                                            )).setScale(2, RoundingMode.CEILING).toString()

                                tv_salebillrowitem_right_gold_amount.text = totalAmt

                            }
                            "gross"->{
                                val goldRate: BigDecimal = salesbill_item_rowModel.item_rate.toBigDecimal()
                                val grossWeight: BigDecimal = salesbill_item_rowModel.item_gross_wt.toBigDecimal()
                                val totalAmt: String =
                                    ((goldRate.setScale(3)
                                        .multiply(grossWeight.setScale(3, RoundingMode.CEILING))
                                            )).setScale(2, RoundingMode.CEILING).toString()

                                tv_salebillrowitem_right_gold_amount.text = totalAmt
                            }
                            "fix"->{
                                val goldRate: BigDecimal = salesbill_item_rowModel.item_rate.toBigDecimal()
                                val totalAmt :String = goldRate.toString()
                                tv_salebillrowitem_right_gold_amount.text = totalAmt
                            }
                        }

                    }
                    false -> {
                        val goldRate: BigDecimal = salesbill_item_rowModel.item_rate.toBigDecimal()
                        val unit: BigDecimal = salesbill_item_rowModel.item_quantity.toBigDecimal()
                        val totalAmt: String =
                            ((goldRate.setScale(3)
                                .multiply(unit.setScale(3, RoundingMode.CEILING))
                                    )).setScale(2, RoundingMode.CEILING).toString()
                        tv_salebillrowitem_right_gold_amount.text = totalAmt
                    }
                }

                when (salesbill_item_rowModel.item_remarks.isNullOrBlank()) {
                    true -> {
                        tv_salebillrowitem_left_remarks.visibility = View.GONE
                    }
                    false -> {
                        tv_salebillrowitem_left_remarks.visibility = View.VISIBLE
                        tv_salebillrowitem_left_remarks.setText("Note: " + salesbill_item_rowModel.item_remarks)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.row_item, parent, false)
        )

    override fun getItemCount(): Int = saledetailitemlist.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(saledetailitemlist[position])
    }

    fun addsalebillrow_item(salesbillrowitemList: List<SaleDetailModel.Item1427117511>) {
        this.saledetailitemlist.apply {
            clear()
            if (salesbillrowitemList != null) {
                addAll(salesbillrowitemList)
            }
        }

    }
}

