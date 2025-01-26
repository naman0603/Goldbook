package com.goldbookapp.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.ShippingOrOtherAddressModel
import com.goldbookapp.ui.activity.AddContactOrAddressActivity
import com.goldbookapp.ui.activity.NewAddressDetailsActivity
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import hk.ids.gws.android.sclick.SClick
import kotlinx.android.synthetic.main.new_customer_activity.*
import kotlinx.android.synthetic.main.row_salepurchase_trans.view.*
import kotlinx.android.synthetic.main.row_sales.view.*
import kotlinx.android.synthetic.main.row_tcs_tds_nog_nop.view.*
import java.lang.StringBuilder

// common adapter for nog/nop
class AddtionalInfoContOtherShipAdapter(
    private val list_Cust_Cont_Ship_Other: ArrayList<ShippingOrOtherAddressModel>?,
    private val list_Supp_Cont_Ship_Other: ArrayList<ShippingOrOtherAddressModel>?
) : RecyclerView.Adapter<AddtionalInfoContOtherShipAdapter.DataViewHolder>(){

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bind(
            custitemModel: ShippingOrOtherAddressModel?,
            suppItemModel: ShippingOrOtherAddressModel?,
            position: Int
        ) {
            itemView.apply {

                when(custitemModel == null) {
                    true -> {
                        // supp model
                        when(suppItemModel!!.type){
                            "contact" ->{
                                ll_AI_row4.visibility = View.VISIBLE

                                txtSectionnognop.setText("${suppItemModel.contact_salutation} ${suppItemModel.contact_first_name} ${suppItemModel.contact_last_name}")

                                txtSectionLabel.setText("Position:")
                                txtRateIndividualName.setText("Phone #:")
                                txtRateOtherName.setText("Email:")
                                txtContactNotesLabel.setText("Notes:")

                                txtSectionValue.setText(suppItemModel.contact_designation)
                                txtRateIndividualValue.setText(suppItemModel.contact_mobile_number)
                                txtRateOtherValue.setText(suppItemModel.contact_email)
                                txtContactNotesValue.setText(suppItemModel.contact_notes)

                            }
                            "address" ->{
                                when(suppItemModel.sub_type){
                                    "shipping" ->{
                                        ll_AI_row2.visibility = View.GONE
                                        ll_AI_row3.visibility = View.GONE
                                        ll_AI_row4.visibility = View.GONE
                                        txtSectionnognop.setText("Shipping Address")
                                        txtSectionLabel.setText("Attn:")
                                        txtSectionValue.setText(suppItemModel.address_contact_name)
                                        tv_shipping_other_address.visibility = View.VISIBLE

                                        var addressStringBuilder: StringBuilder = StringBuilder()
                                        addressStringBuilder
                                            .append(suppItemModel.address_line_1.toString().trim()).append(", ")
                                            .append(suppItemModel.address_line_2.toString().trim()).append(", ")
                                            .append(suppItemModel.address_landmark.toString().trim()).append(", ")
                                            .append(suppItemModel.address_country_name.toString().trim()).append(", ")
                                            .append(suppItemModel.address_state_name.toString().trim()).append(", ")
                                            .append(suppItemModel.address_city_name.toString().trim()).append(", ")
                                            .append(suppItemModel.address_mobile_number.toString().trim()).append(", ")

                                        tv_shipping_other_address.text = CommonUtils.removeUnwantedComma(addressStringBuilder.toString())

                                    }
                                    "other" ->{
                                        ll_AI_row2.visibility = View.GONE
                                        ll_AI_row3.visibility = View.GONE
                                        ll_AI_row4.visibility = View.GONE
                                        txtSectionnognop.setText("Other Address")
                                        txtSectionLabel.setText("Attn:")
                                        txtSectionValue.setText(suppItemModel.address_contact_name)
                                        tv_shipping_other_address.visibility = View.VISIBLE

                                        var addressStringBuilder: StringBuilder = StringBuilder()
                                        addressStringBuilder
                                            .append(suppItemModel.address_line_1.toString().trim()).append(", ")
                                            .append(suppItemModel.address_line_2.toString().trim()).append(", ")
                                            .append(suppItemModel.address_landmark.toString().trim()).append(", ")
                                            .append(suppItemModel.address_country_name.toString().trim()).append(", ")
                                            .append(suppItemModel.address_state_name.toString().trim()).append(", ")
                                            .append(suppItemModel.address_city_name.toString().trim()).append(", ")
                                            .append(suppItemModel.address_mobile_number.toString().trim()).append(", ")

                                        tv_shipping_other_address.text = CommonUtils.removeUnwantedComma(addressStringBuilder.toString())
                                    }
                                }
                            }
                        }


                        // edit supp
                        imgRight2nognop.clickWithDebounce {

                            imgRight2nognop.context.startActivity(
                                Intent(imgRight2nognop.context, NewAddressDetailsActivity::class.java)
                                    .putExtra(Constants.EDIT_SUPP_POS_KEY, position)
                                    .putExtra(Constants.isFromEditCustAddress,false)
                                    .putExtra(Constants.PREF_ADD_EDIT_SUPP_SHIPPING_OTHER_KEY, Gson().toJson(custitemModel)))

                        }
                        imgRightnognop.clickWithDebounce {
                            (context as AddContactOrAddressActivity).removeAddressItem(position,false)
                        }

                    }
                    false ->{
                        // cust model
                        when(custitemModel.type){
                            "contact" ->{
                                ll_AI_row4.visibility = View.VISIBLE

                                txtSectionnognop.setText("${custitemModel.contact_salutation} ${custitemModel.contact_first_name} ${custitemModel.contact_last_name}")




                                txtSectionLabel.setText("Position:")
                                txtRateIndividualName.setText("Phone #:")
                                txtRateOtherName.setText("Email:")
                                txtContactNotesLabel.setText("Notes:")

                                txtSectionValue.setText(custitemModel.contact_designation)
                                txtRateIndividualValue.setText(custitemModel.contact_mobile_number)
                                txtRateOtherValue.setText(custitemModel.contact_email)
                                txtContactNotesValue.setText(custitemModel.contact_notes)

                            }
                            "address" ->{
                                when(custitemModel.sub_type){
                                    "shipping" ->{
                                        ll_AI_row2.visibility = View.GONE
                                        ll_AI_row3.visibility = View.GONE
                                        ll_AI_row4.visibility = View.GONE
                                        txtSectionnognop.setText("Shipping Address")
                                        txtSectionLabel.setText("Attn:")
                                        txtSectionValue.setText(custitemModel.address_contact_name)
                                        tv_shipping_other_address.visibility = View.VISIBLE

                                        var addressStringBuilder: StringBuilder = StringBuilder()
                                        addressStringBuilder
                                            .append(custitemModel.address_line_1.toString().trim()).append(", ")
                                            .append(custitemModel.address_line_2.toString().trim()).append(", ")
                                            .append(custitemModel.address_landmark.toString().trim()).append(", ")
                                            .append(custitemModel.address_country_name.toString().trim()).append(", ")
                                            .append(custitemModel.address_state_name.toString().trim()).append(", ")
                                            .append(custitemModel.address_city_name.toString().trim()).append(", ")
                                            .append(custitemModel.address_mobile_number.toString().trim()).append(", ")

                                        tv_shipping_other_address.text = CommonUtils.removeUnwantedComma(addressStringBuilder.toString())

                                    }
                                    "other" ->{
                                        ll_AI_row2.visibility = View.GONE
                                        ll_AI_row3.visibility = View.GONE
                                        ll_AI_row4.visibility = View.GONE
                                        txtSectionnognop.setText("Other Address")
                                        txtSectionLabel.setText("Attn:")
                                        txtSectionValue.setText(custitemModel.address_contact_name)
                                        tv_shipping_other_address.visibility = View.VISIBLE

                                        var addressStringBuilder: StringBuilder = StringBuilder()
                                        addressStringBuilder
                                            .append(custitemModel.address_line_1.toString().trim()).append(", ")
                                            .append(custitemModel.address_line_2.toString().trim()).append(", ")
                                            .append(custitemModel.address_landmark.toString().trim()).append(", ")
                                            .append(custitemModel.address_country_name.toString().trim()).append(", ")
                                            .append(custitemModel.address_state_name.toString().trim()).append(", ")
                                            .append(custitemModel.address_city_name.toString().trim()).append(", ")
                                            .append(custitemModel.address_mobile_number.toString().trim()).append(", ")

                                        tv_shipping_other_address.text = CommonUtils.removeUnwantedComma(addressStringBuilder.toString())
                                    }
                                }
                            }
                        }


                        // edit cust
                        imgRight2nognop.clickWithDebounce {

                            imgRight2nognop.context.startActivity(
                                Intent(imgRight2nognop.context, NewAddressDetailsActivity::class.java)
                                    .putExtra(Constants.EDIT_CUST_POS_KEY, position)
                                    .putExtra(Constants.isFromEditCustAddress,true)
                                    .putExtra(Constants.PREF_ADD_EDIT_CUST_SHIPPING_OTHER_KEY, Gson().toJson(custitemModel)))

                        }
                        imgRightnognop.clickWithDebounce {
                            (context as AddContactOrAddressActivity).removeAddressItem(position,true)
                        }
                    }
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_tcs_tds_nog_nop, parent, false))





    override fun getItemCount(): Int = when(list_Cust_Cont_Ship_Other.isNullOrEmpty()) {
        true -> {
            when(list_Supp_Cont_Ship_Other.isNullOrEmpty()){
                true -> 0
                false -> list_Supp_Cont_Ship_Other.size
            }

        }
        false -> list_Cust_Cont_Ship_Other.size
    }


    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        when(list_Cust_Cont_Ship_Other.isNullOrEmpty()) {
            true -> holder.bind(null,this.list_Supp_Cont_Ship_Other!![position],position)
            false -> holder.bind(this.list_Cust_Cont_Ship_Other[position],null,position)
        }

    }

    fun addCustAddInfo(custAddInfoList: List<ShippingOrOtherAddressModel>?) {
        this.list_Cust_Cont_Ship_Other.apply {
            this!!.clear()
            if (custAddInfoList != null) {
                addAll(custAddInfoList)
            }
        }

    }
    fun addSuppAddInfo(suppAddInfoList: List<ShippingOrOtherAddressModel>?) {
        this.list_Supp_Cont_Ship_Other.apply {
            this!!.clear()
            if (suppAddInfoList != null) {
                addAll(suppAddInfoList)
            }
        }

    }
}

