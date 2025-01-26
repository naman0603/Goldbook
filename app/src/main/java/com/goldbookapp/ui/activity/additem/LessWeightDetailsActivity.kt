package com.goldbookapp.ui.activity.additem

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldbookapp.R
import com.goldbookapp.databinding.ActivityLessWeightDetailBinding
import com.goldbookapp.model.AddLessWeightModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.OpeningStockItemModel
import com.goldbookapp.ui.adapter.LessWeightAdapter
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_cheque_register.*
import kotlinx.android.synthetic.main.activity_less_weight_detail.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.math.RoundingMode

class LessWeightDetailsActivity : AppCompatActivity() {

    lateinit var binding: ActivityLessWeightDetailBinding
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel

    private lateinit var adapter: LessWeightAdapter
    var is_from_new_less_weight: Boolean = true
    lateinit var lessweightList: ArrayList<AddLessWeightModel.AddLessWeightModelItem>
    lateinit var lessweightEditList: ArrayList<AddLessWeightModel.AddLessWeightModelItem>
    var totalLwofAllUpdatedValue: String = "0.00"
    var totalLwAmtofAllUpdatedValue: String = "0.00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_less_weight_detail)
        setupUIandListner()
    }

    private fun setupUIandListner() {

        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText("Less Weight Details")

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        tvAddLessWeight?.clickWithDebounce {

            startActivity(
                Intent(this, AddLessWeightActivity::class.java)
            )
        }


        if (prefs.contains(Constants.PREF_LESS_WEIGHT_INFO_KEY)) {
            // prefs.edit().remove(Constants.PREF_CHEQUE_BOOK_KEY).apply()
            val collectionType =
                object :
                    TypeToken<ArrayList<AddLessWeightModel.AddLessWeightModelItem>>() {}.type
            lessweightList =
                Gson().fromJson(
                    prefs[Constants.PREF_LESS_WEIGHT_INFO_KEY, ""],
                    collectionType
                )

            val chequeArray: String = Gson().toJson(lessweightList)

            if (lessweightList != null) {
                if(lessweightList.get(0).less_wt_item_name.isNotBlank()){
                    ly_less_weight_details.visibility = VISIBLE
                    recyclerViewLessWeight.layoutManager = LinearLayoutManager(this)
                    adapter = LessWeightAdapter(lessweightList,false)
                    binding.recyclerViewLessWeight.setHasFixedSize(true)
                    binding.recyclerViewLessWeight.adapter = adapter
                }
            }
        }

        /*if (intent.extras?.containsKey(Constants.IS_FROM_NEW_INVOICE_LESS_WEIGHT)!!) {
            is_from_new_less_weight =
                intent.getBooleanExtra(Constants.IS_FROM_NEW_INVOICE_LESS_WEIGHT, false)

            if (is_from_new_less_weight) {

            }
            else{

            }

        }*/

    }


    override fun onResume() {
        super.onResume()

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) {
                handleConnectivityChange()
            }
        })
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected) {
            // Network is available
            CommonUtils.hideInternetDialog()

            if (is_from_new_less_weight) {
                //true->add Less weight Item
                if (prefs.contains(Constants.PREF_LESS_WEIGHT_INFO_KEY)) {
                    val collectionType =
                        object :
                            TypeToken<ArrayList<AddLessWeightModel.AddLessWeightModelItem>>() {}.type
                    lessweightList =
                        Gson().fromJson(prefs[Constants.PREF_LESS_WEIGHT_INFO_KEY, ""], collectionType)


                    val chequeArray: String = Gson().toJson(lessweightList)

                    if (lessweightList != null) {
                        ly_less_weight_details.visibility = VISIBLE
                        recyclerViewLessWeight.layoutManager = LinearLayoutManager(this)
                        adapter = LessWeightAdapter(lessweightList, false)
                        binding.recyclerViewLessWeight.setHasFixedSize(true)
                        binding.recyclerViewLessWeight.adapter = adapter
                    }
                }
            }else{
                //false-> Edit Less Weight

                if (prefs.contains(Constants.PREF_LESS_WEIGHT_INFO_EDITKEY)) {
                    val collectionType =
                        object :
                            TypeToken<ArrayList<AddLessWeightModel.AddLessWeightModelItem>>() {}.type
                    lessweightEditList =
                        Gson().fromJson(
                            prefs[Constants.PREF_LESS_WEIGHT_INFO_EDITKEY, ""],
                            collectionType
                        )


                    // val chequeArray: String = Gson().toJson(chequeEditList)

                    if (lessweightEditList != null) {
                        ly_cheque_details.visibility = VISIBLE
                        recyclerViewChequeRegister.layoutManager = LinearLayoutManager(this)
                        adapter = LessWeightAdapter(lessweightEditList, true)
                        binding.recyclerViewLessWeight.setHasFixedSize(true)
                        binding.recyclerViewLessWeight.adapter = adapter

                    }
                }
            }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
        }
    }


    fun deleteLessWeight(position: Int, is_from_new: Boolean) {
        when (is_from_new) {
            //from add
            true -> {
                if (lessweightList != null && lessweightList!!.size > 0) {
                    if (position >= lessweightList!!.size) {
                        //index not exists
                    } else {
                        // index exists

                        lessweightList!!.removeAt(position)
                        adapter.notifyDataSetChanged()



                        if (lessweightList!!.size > 0) {
                            prefs[Constants.PREF_LESS_WEIGHT_INFO_KEY] = Gson().toJson(lessweightList)
                            saveItemWtBreakupModel()
                            // invoiceCalculation()
                        } else {
                            prefs.edit().remove(Constants.PREF_LESS_WEIGHT_INFO_KEY).apply()
                            prefs.edit().remove(Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY).apply()
                            finish()
                            //  linear_calculation_view_purchase.visibility = View.GONE
                        }
                    }
                }

            }
            //from edit
            false -> {
                if (lessweightList != null && lessweightList!!.size > 0) {
                    if (position >= lessweightList!!.size) {
                        //index not exists
                    } else {
                        // index exists
                        lessweightList!!.removeAt(position)
                        adapter.notifyDataSetChanged()

                        if (lessweightList!!.size > 0) {
                            prefs[Constants.PREF_LESS_WEIGHT_INFO_KEY] =
                                Gson().toJson(lessweightList)
                            // invoiceCalculation()
                        } else {
                            prefs.edit().remove(Constants.PREF_LESS_WEIGHT_INFO_KEY).apply()
                            finish()
                            //  linear_calculation_view_purchase.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun saveItemWtBreakupModel() {
        calculateTotalofLessWtTotalnAmt()
        val childModel = OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup(
            lessweightList.size.toString(),
            lessweightList,
            totalLwofAllUpdatedValue,
            totalLwAmtofAllUpdatedValue

        )

        prefs[Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY] = Gson().toJson(childModel)

    }

    private fun calculateTotalofLessWtTotalnAmt() {
        for (lesswt in lessweightList) {
            totalLwofAllUpdatedValue =
                ((lesswt.less_wt_final_wt.toBigDecimal().setScale(2)
                    .plus(totalLwofAllUpdatedValue.toBigDecimal().setScale(2))
                        )).setScale(2, RoundingMode.CEILING).toString()

            totalLwAmtofAllUpdatedValue =
                ((lesswt.less_wt_total_amount.toBigDecimal().setScale(2)
                    .plus(totalLwAmtofAllUpdatedValue.toBigDecimal().setScale(2))
                        )).setScale(2, RoundingMode.CEILING).toString()
        }
    }


}