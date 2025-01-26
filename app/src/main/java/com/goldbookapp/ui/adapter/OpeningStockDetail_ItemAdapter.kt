package com.goldbookapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.CalculateSalesModel
import kotlinx.android.synthetic.main.row_item.view.*
import java.lang.StringBuilder

class OpeningStockDetail_ItemAdapter(private val purchasedetailitemlist: ArrayList<CalculateSalesModel.Datac.Item558106789>) : RecyclerView.Adapter<OpeningStockDetail_ItemAdapter.DataViewHolder>(){

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(purchasebill_item_rowModel: CalculateSalesModel.Datac.Item558106789) {
            itemView.apply {

                iv_salebillrowitem_deleteitem.visibility = View.GONE

                tv_salebillrowitem_left_item_name.text= purchasebill_item_rowModel.item_name.toString()

                when(purchasebill_item_rowModel.quantity!!.toInt()>0){
                    true->{
                        tv_salebillrowitem_right_itemquantity.visibility = View.VISIBLE
                        tv_salebillrowitem_right_itemquantity.text=purchasebill_item_rowModel.quantity.toString() + " "+purchasebill_item_rowModel.unit_name
                    }
                    false->{
                        tv_salebillrowitem_right_itemquantity.visibility = View.GONE
                            }
                }

                tv_salebillrowitem_right_fine_wt.text=purchasebill_item_rowModel.fine_wt.toString() + " (F)"


                if(purchasebill_item_rowModel.charge!= null && purchasebill_item_rowModel.charge?.size!! > 0) {
                    var charge_name: StringBuilder = StringBuilder()
                    var charge_amount: StringBuilder = StringBuilder()
                    var count: Int = 0
                    for (item in purchasebill_item_rowModel.charge!!) {
                        // body of loop

                        if (count == purchasebill_item_rowModel.charge.size - 1) {
                            charge_name.append(item.name?.trim().toString())
                            charge_amount.append(" ₹ ").append(item.total_amount?.trim().toString())
                        } else {
                            charge_name.append(item.name?.trim().toString()).append("\n")
                            charge_amount.append(" ₹ ").append(item.total_amount?.trim().toString()).append("\n")
                        }

                        count++
                    }
                    tv_salebillrowitem_left_chargetypeone.visibility=View.VISIBLE
                    tv_salebillrowitem_right_amountone.visibility=View.VISIBLE
                    tv_salebillrowitem_left_chargetypeone.text = charge_name.toString()
                    tv_salebillrowitem_right_amountone.text = charge_amount.toString()

                }
                tv_salebillrowitem_left_itemcalculation.text=purchasebill_item_rowModel.gross_wt.toString() +
                        " - "+("%.3f".format(purchasebill_item_rowModel.total_less_wt?.toDouble()))+"\n" +
                        "= "+purchasebill_item_rowModel.net_wt+" x "+purchasebill_item_rowModel.cost

                when(purchasebill_item_rowModel.remarks.isNullOrBlank()){
                    true->{
                        tv_salebillrowitem_left_remarks.visibility = View.GONE
                    }
                    false->{
                        tv_salebillrowitem_left_remarks.visibility = View.VISIBLE
                        tv_salebillrowitem_left_remarks.setText("Note: "+purchasebill_item_rowModel.remarks)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_item, parent, false))

    override fun getItemCount(): Int = purchasedetailitemlist.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(purchasedetailitemlist[position])
    }

    fun addpurchasebillrow_item(purchasebillrowitemList: List<CalculateSalesModel.Datac.Item558106789>?) {
        this.purchasedetailitemlist.apply {
            clear()
            if (purchasebillrowitemList != null) {
                addAll(purchasebillrowitemList)
            }
        }

    }
}

