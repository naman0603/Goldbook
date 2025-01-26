package com.goldbookapp.ui.activity.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.goldbookapp.R
import com.goldbookapp.databinding.ActivityTcsTdsNatureBinding
import com.goldbookapp.model.TaxDetailTcsModel
import com.goldbookapp.model.TaxDetailTdsModel
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.DigitsInputFilter
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_tcs_tds_nature.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class TCSNatureActivity : AppCompatActivity() {
    private var nog_nop_type : String? = "nog"
    lateinit var prefs: SharedPreferences
    var addNogList = ArrayList<TaxDetailTcsModel.Data.Nature_of_goods>()
    var addNopList = ArrayList<TaxDetailTdsModel.Data.Nature_of_payment>()
    var selectedNogID: String? = null
    var selectedNopID: String? = null

    private var isFromTcsNogAdd : Boolean = true
    private var isEditTcsNog : Boolean = false
    private var isEditTdsNop : Boolean = false

    private var edit_nog: Int = 0
    private var edit_nop: Int = 0
    lateinit var binding: ActivityTcsTdsNatureBinding
    lateinit var tcsNogModel :TaxDetailTcsModel.Data.Nature_of_goods
    lateinit var tdsNopModel : TaxDetailTdsModel.Data.Nature_of_payment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tcs_tds_nature)
        setupUIandListner()
    }
    private fun setupUIandListner() {
        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.tcs_nature)

        prefs = PreferenceHelper.defaultPrefs(this)

        imgLeft?.clickWithDebounce{

            onBackPressed()
        }
        applyDigitInputFilterToFileds()
        // to get nog/nop type out of two
        if(intent.extras?.containsKey(Constants.NOG_NOP_TYPE)!!) {
            nog_nop_type = intent.getStringExtra(Constants.NOG_NOP_TYPE)
        }

        if(intent.extras?.containsKey(Constants.isFromTcsNogAdd)!!){
            isFromTcsNogAdd = intent.getBooleanExtra(Constants.isFromTcsNogAdd,true)
            when(isFromTcsNogAdd) {
                true -> {
                    tvTitle.setText(R.string.tcs_nature)
                    selectedNogID = null
                }
                false -> {
                    tvTitle.setText(R.string.tds_nature)
                    selectedNopID = null
                }
            }

        }
        // is from edit tcs_nog or tds_nop
        else{
            if(intent.extras?.containsKey(Constants.TCS_TDS_NOG_NOP_KEY)!!){
                when(nog_nop_type){
                    "nog" -> {
                        tvTitle.setText(R.string.tcs_nature)
                        val tcsnog_str: String? = intent.getStringExtra(Constants.TCS_TDS_NOG_NOP_KEY)
                        tcsNogModel= Gson().fromJson(
                            tcsnog_str,
                            TaxDetailTcsModel.Data.Nature_of_goods::class.java
                        )
                        selectedNogID = tcsNogModel.nature_of_goods_id
                        isEditTcsNog = true
                        isEditTdsNop = false
                        edit_nog = intent.getIntExtra(Constants.PREF_EDIT_NOG_KEY,0)
                        setNogFiedls()

                    }
                    "nop" -> {
                        tvTitle.setText(R.string.tds_nature)
                        val tdsnop_str: String? = intent.getStringExtra(Constants.TCS_TDS_NOG_NOP_KEY)
                        tdsNopModel= Gson().fromJson(
                            tdsnop_str,
                            TaxDetailTdsModel.Data.Nature_of_payment::class.java
                        )
                        selectedNopID = tdsNopModel.nature_of_payment_id
                        isEditTcsNog = false
                        isEditTdsNop = true
                        edit_nop = intent.getIntExtra(Constants.PREF_EDIT_NOP_KEY,0)
                        setNopFiedls()
                    }
                }

            }
        }


        btnSaveAdd_TcsTds?.clickWithDebounce {

            if (performValidation()) {
                saveNogNopModel(nog_nop_type)
                when(nog_nop_type){
                    "nog" -> {
                        startActivity(
                            Intent(
                                this,
                                TCSNatureActivity::class.java
                            ).putExtra(Constants.NOG_NOP_TYPE,"nog" ).putExtra(Constants.isFromTcsNogAdd, true)
                        )
                        finish()
                    }
                    "nop" -> {
                        startActivity(
                            Intent(
                                this,
                                TCSNatureActivity::class.java
                            ).putExtra(Constants.NOG_NOP_TYPE,"nop" ).putExtra(Constants.isFromTcsNogAdd, false)
                        )
                        finish()
                    }
                }
            }
        }
        btnSaveCloseTcsTds?.clickWithDebounce {

            if (performValidation()) {
                saveNogNopModel(nog_nop_type)
                finish()
            }
        }
    }

    private fun applyDigitInputFilterToFileds() {
        txtWithPAN.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    3,
                    3,
                    100.00
                )
            )
        )
        txtWithoutPAN.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    3,
                    3,
                    100.00
                )
            )
        )
        txtWithPANRate.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    3,
                    3,
                    100.00
                )
            )
        )
        txtWithoutPANRate.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    3,
                    3,
                    100.00
                )
            )
        )
    }

    private fun saveNogNopModel(nog_nop_type: String?) {
        when(nog_nop_type){
            "nog" -> {
                if (prefs.contains(Constants.PREF_ADD_NOG_KEY)) {
                    val collectionType = object :
                        TypeToken<java.util.ArrayList<TaxDetailTcsModel.Data.Nature_of_goods>>() {}.type
                    val nog_List: ArrayList<TaxDetailTcsModel.Data.Nature_of_goods> =
                        Gson().fromJson(prefs[Constants.PREF_ADD_NOG_KEY, ""], collectionType)
                    addNogList.addAll(nog_List)
                } else {
                    addNogList = ArrayList()
                }

                val childItemModel = TaxDetailTcsModel.Data.Nature_of_goods(
                    selectedNogID,
                    txtNameTCSTDS.text.toString().trim(),
                    txtSectionTCSTDS.text.toString().trim(),
                    txtPayCodeNature.text.toString().trim(),
                    txtWithPAN.text.toString().trim(),
                    txtWithoutPAN.text.toString().trim(),
                    txtWithPANRate.text.toString().trim(),
                    txtWithoutPANRate.text.toString().trim()
                )


                if (isEditTcsNog == true) {
                    // Update item
                    addNogList.set(edit_nog, childItemModel)
                } else {
                    // Add new item
                    addNogList.add(childItemModel)
                }
                val prefs = PreferenceHelper.defaultPrefs(this)
                prefs[Constants.PREF_ADD_NOG_KEY] = Gson().toJson(addNogList)
            }
            "nop" -> {
                if (prefs.contains(Constants.PREF_ADD_NOP_KEY)) {
                    val collectionType = object :
                        TypeToken<java.util.ArrayList<TaxDetailTdsModel.Data.Nature_of_payment>>() {}.type
                    val nop_List: ArrayList<TaxDetailTdsModel.Data.Nature_of_payment> =
                        Gson().fromJson(prefs[Constants.PREF_ADD_NOP_KEY, ""], collectionType)
                    addNopList.addAll(nop_List)
                } else {
                    addNopList = ArrayList()
                }

                val childItemModel = TaxDetailTdsModel.Data.Nature_of_payment(
                    selectedNopID,
                    txtNameTCSTDS.text.toString().trim(),
                    txtSectionTCSTDS.text.toString().trim(),
                    txtPayCodeNature.text.toString().trim(),
                    txtWithPAN.text.toString().trim(),
                    txtWithoutPAN.text.toString().trim(),
                    txtWithPANRate.text.toString().trim(),
                    txtWithoutPANRate.text.toString().trim()
                )



                if (isEditTdsNop == true) {
                    // Update item
                    addNopList.set(edit_nop, childItemModel)
                } else {
                    // Add new item
                    addNopList.add(childItemModel)
                }
                val prefs = PreferenceHelper.defaultPrefs(this)
                prefs[Constants.PREF_ADD_NOP_KEY] = Gson().toJson(addNopList)
            }
        }

    }

    fun performValidation(): Boolean {
        if (txtNameTCSTDS.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.nognopname))
            txtNameTCSTDS.requestFocus()
            return false
        }else if (txtSectionTCSTDS.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.nognopsection))
            txtSectionTCSTDS.requestFocus()
            return false
        }else if (txtPayCodeNature.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.nognoppaymentcode))
            txtPayCodeNature.requestFocus()
            return false
        }else if (txtWithPAN.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.nognopratewithpan))
            txtWithPAN.requestFocus()
            return false
        }else if (txtWithoutPAN.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.nognopratewithoutpan))
            txtWithoutPAN.requestFocus()
            return false
        }else if (txtWithPANRate.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.nognopratewithotherpan))
            txtWithPANRate.requestFocus()
            return false
        }
        else if (txtWithoutPANRate.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.nognopratewithoutotherpan))
            txtWithoutPANRate.requestFocus()
            return false
        }
        return true


    }

    // edit tcs nog
    private fun setNogFiedls() {
        txtNameTCSTDS.setText(tcsNogModel.name)
        txtSectionTCSTDS.setText(tcsNogModel.section)
        txtPayCodeNature.setText(tcsNogModel.payment_code)
        txtWithPAN.setText(tcsNogModel.rate_with_pan)
        txtWithoutPAN.setText(tcsNogModel.rate_without_pan)
        txtWithPANRate.setText(tcsNogModel.rate_other_with_pan)
        txtWithoutPANRate.setText(tcsNogModel.rate_other_without_pan)
    }
    // edit tds nop
    private fun setNopFiedls() {
        txtNameTCSTDS.setText(tdsNopModel.name)
        txtSectionTCSTDS.setText(tdsNopModel.section)
        txtPayCodeNature.setText(tdsNopModel.payment_code)
        txtWithPAN.setText(tdsNopModel.rate_with_pan)
        txtWithoutPAN.setText(tdsNopModel.rate_without_pan)
        txtWithPANRate.setText(tdsNopModel.rate_other_with_pan)
        txtWithoutPANRate.setText(tdsNopModel.rate_other_without_pan)
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

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
        }
    }
}