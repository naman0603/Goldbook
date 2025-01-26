package com.goldbookapp.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.TaxDetailTcsModel
import com.goldbookapp.model.TaxDetailTdsModel
import com.goldbookapp.ui.activity.settings.TCSActivity
import com.goldbookapp.ui.activity.settings.TCSNatureActivity
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.Constants.Companion.NOG_NOP_TYPE
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_tcs_tds_nog_nop.view.*

// common adapter for nog/nop
class TcsNatureOfGoodsAdapter(
    private val natureofgoodsList: ArrayList<TaxDetailTcsModel.Data.Nature_of_goods>?,
    private val listNop: ArrayList<TaxDetailTdsModel.Data.Nature_of_payment>?,
    private val saveTaxbtnShow: Boolean
) : RecyclerView.Adapter<TcsNatureOfGoodsAdapter.DataViewHolder>() {

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        private lateinit var adapter: SalePurchaseReportPrintSubAdapter

        fun bind(
            nogitemModel: TaxDetailTcsModel.Data.Nature_of_goods?,
            nopItemModel: TaxDetailTdsModel.Data.Nature_of_payment?,
            position: Int,
            saveTaxbtnShow: Boolean

        ) {
            itemView.apply {

                when (saveTaxbtnShow) {
                    true -> {
                        imgRight2nognop.visibility = View.VISIBLE
                        imgRightnognop.visibility = View.VISIBLE

                    }
                    false->{
                        imgRight2nognop.visibility = View.GONE
                        imgRightnognop.visibility = View.GONE
                    }

                }
                when (nogitemModel == null) {
                    true -> {
                        // nop
                        txtSectionnognop.text = nopItemModel!!.name
                        txtSectionValue.text = nopItemModel.section
                        txtRateIndividualValue.text = nopItemModel.rate_with_pan
                        txtRateOtherValue.text = nopItemModel.rate_other_with_pan
                        ll_AI_row4.visibility = View.GONE
                        // edit nop
                        imgRight2nognop.clickWithDebounce {

                            imgRight2nognop.context.startActivity(
                                Intent(
                                    imgRight2nognop.context,
                                    TCSNatureActivity::class.java
                                )
                                    .putExtra(Constants.PREF_EDIT_NOP_KEY, position)
                                    .putExtra(NOG_NOP_TYPE, "nop")
                                    .putExtra(
                                        Constants.TCS_TDS_NOG_NOP_KEY,
                                        Gson().toJson(nopItemModel)
                                    )
                            )

                        }
                        imgRightnognop.clickWithDebounce {
                            (context as TCSActivity).removeNopNogItem(position, false)
                        }

                    }
                    false -> {
                        // nog
                        txtSectionnognop.text = nogitemModel.name
                        txtSectionValue.text = nogitemModel.section
                        txtRateIndividualValue.text = nogitemModel.rate_with_pan
                        txtRateOtherValue.text = nogitemModel.rate_other_with_pan
                        ll_AI_row4.visibility = View.GONE
                        // edit nog
                        imgRight2nognop.clickWithDebounce {

                            imgRight2nognop.context.startActivity(
                                Intent(
                                    imgRight2nognop.context,
                                    TCSNatureActivity::class.java
                                )
                                    .putExtra(Constants.PREF_EDIT_NOG_KEY, position)
                                    .putExtra(NOG_NOP_TYPE, "nog")
                                    .putExtra(
                                        Constants.TCS_TDS_NOG_NOP_KEY,
                                        Gson().toJson(nogitemModel)
                                    )
                            )

                        }
                        imgRightnognop.clickWithDebounce {

                            (context as TCSActivity).removeNopNogItem(position, true)
                        }
                    }
                }

            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.row_tcs_tds_nog_nop, parent, false)
        )


    override fun getItemCount(): Int = when (natureofgoodsList.isNullOrEmpty()) {
        true -> {
            when (listNop.isNullOrEmpty()) {
                true -> 0
                false -> listNop.size
            }

        }
        false -> natureofgoodsList.size
    }


    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        when (natureofgoodsList.isNullOrEmpty()) {
            true -> holder.bind(null, this.listNop!![position], position, saveTaxbtnShow)
            false -> holder.bind(this.natureofgoodsList[position], null, position, saveTaxbtnShow)
        }

    }

    fun addNog(nogList: List<TaxDetailTcsModel.Data.Nature_of_goods>?) {
        this.natureofgoodsList.apply {
            this!!.clear()
            if (nogList != null) {
                addAll(nogList)
            }
        }

    }

    fun addNop(nopList: List<TaxDetailTdsModel.Data.Nature_of_payment>?) {
        this.listNop.apply {
            this!!.clear()
            if (nopList != null) {
                addAll(nopList)
            }
        }

    }
}

