package com.goldbookapp.ui.activity.settings

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityGstBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.SettingsGstDetailModel
import com.goldbookapp.model.StateModel
import com.goldbookapp.ui.activity.viewmodel.SaveTaxesViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_gst.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.util.*

class GSTActivity : AppCompatActivity() {
    private var saveTaxbtnShow: Boolean = false
    var c = Calendar.getInstance()
    var stateList: List<StateModel.Data.State693361839>? = null
    var stateNameList: List<String>? = null
    var selectedStateID: String? = null
    lateinit var stateNameAdapter: ArrayAdapter<String>
    lateinit var binding: ActivityGstBinding
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    private lateinit var viewModel: SaveTaxesViewModel
    var enable_gst: Int = 0
    var periodicity_of_gst1: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gst)
        setupUIandListner()
        setupViewModel()
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
            when(loginModel.data!!.user_info!!.user_type.equals("user",true)){
                // user type user
                true -> {
                    // apply restriciton
                    defaultDisableAllButtonnUI()
                    if (intent.extras != null && intent.extras!!.containsKey(Constants.Change_Status)) {
                        saveTaxbtnShow = intent.getBooleanExtra(Constants.Change_Status,false)
                        when(saveTaxbtnShow){
                            true -> {
                                binding.btnSaveSettingGst.visibility = View.VISIBLE
                            }
                            false -> {
                                binding.btnSaveSettingGst.visibility = View.GONE
                            }
                        }
                     //   Log.v("gstact", saveTaxbtnShow.toString())
                    }
                }
                // user_type -> admin or super_admin or any other
                false -> {
                    defaultEnableAllButtonnUI()
                }
            }


            getTaxGSTDetailApi(loginModel.data?.bearer_access_token)
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }


    private fun defaultEnableAllButtonnUI() {
        binding.btnSaveSettingGst.visibility = View.VISIBLE
    }

    private fun defaultDisableAllButtonnUI() {
        binding.btnSaveSettingGst.visibility = View.GONE
    }
    private fun setupUIandListner() {
        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.gst)
       // tvRight.setText(R.string.save)

        getLoginModelFromPrefs()

        imgLeft?.clickWithDebounce {

            onBackPressed()
        }

    }

    fun getState(countryID: String?){

        viewModel.getState(countryID).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        /* Log.v("..setupObservers..", "..Success...")*/
                        if (it.data?.status == true) {
                            stateList = it.data.data?.state

                            stateNameList = stateList?.map { it.name }

                            stateNameAdapter = ArrayAdapter<String>(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                stateNameList!!
                            )


                            binding.txtGstState.setAdapter(stateNameAdapter)
                            binding.txtGstState.threshold = 1

                            binding.txtGstState.setOnItemClickListener { adapterView, view, position, l
                                ->
                                val selected: String =
                                    adapterView.getItemAtPosition(position).toString()
                                val pos: Int? = stateNameList?.indexOf(selected)

                                selectedStateID = pos?.let { it1 -> stateList?.get(it1)?.id }

                            }

                        } else {
                            when(it.data!!.code == Constants.ErrorCode){
                                true-> {
                                    Toast.makeText(
                                        this,
                                        it.data.errormessage?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                }
                                false->{
                                    CommonUtils.somethingWentWrong(this)
                                }

                            }
                        }


                    }
                    Status.ERROR -> {
                        /*Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                            .show()*/
                        /* Log.v("..setupObservers..", "..ERROR...")*/
                    }
                    Status.LOADING -> {
                        /* Log.v("..setupObservers..", "..LOADING...")*/
                    }
                }
            }
        })
    }
    private fun getTaxGSTDetailApi(token: String?) {
        if(NetworkUtils.isConnected()) {
            viewModel.getdetailGstApi(token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                setdefaultGstData(it.data.data)

                            } else {
                                /*Toast.makeText(
                                    this,
                                    it.data?.errormessage?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()*/
                            }
                            CommonUtils.hideProgress()

                        }
                        Status.ERROR -> {
                            CommonUtils.hideProgress()
                            /* Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                                 .show()*/
                        }
                        Status.LOADING -> {
                            CommonUtils.showProgress(this)
                        }
                    }
                }
            })
        }else{
            /*CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))*/
        }
    }

    private fun setdefaultGstData(data: SettingsGstDetailModel.Data?) {
        getState(data!!.country_id)
        switchGstEnableDisable.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                enable_gst = 1
                cardGst.visibility = View.VISIBLE
            } else {
                enable_gst = 0
                cardGst.visibility = View.GONE
            }
        }
        when(data.enable_gst.equals("1")){
            true -> {
                switchGstEnableDisable.isChecked = true
                txtGstRegDate.isEnabled = false
                cardGst.visibility = View.VISIBLE
                switchGstEnableDisable.isEnabled = false
            }
            false -> {
                switchGstEnableDisable.isChecked = false
                cardGst.visibility = View.GONE
                switchGstEnableDisable.isEnabled = true
            }
        }
        binding.txtGstState.setText(data.state_name)
        selectedStateID = data.gst_state_id
        binding.txtGstGstin.setText(data.gstin)
        binding.txtGstRegDate.setText(data.registration_date)

        binding.txtGstRegDate.clickWithDebounce {

            openDatePicker()
        }

        radiogroupGst.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radioGstMonthly.id -> {periodicity_of_gst1 = radioGstMonthly.text.toString().toLowerCase()}
                radioGstQuarter.id -> {periodicity_of_gst1 = radioGstQuarter.text.toString().toLowerCase()}
            }
        })

        periodicity_of_gst1 = data.periodicity_of_gst1

        when(data.periodicity_of_gst1 == "monthly"){
            true -> {
                radioGstMonthly.isChecked = true
                radioGstQuarter.isChecked = false
            }
            false -> {
                radioGstMonthly.isChecked = false
                radioGstQuarter.isChecked = true
            }
        }

        periodicity_of_gst1 = "quarterly"

        btnSaveSettingGst?.clickWithDebounce {

            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    // common for tcs/tds/gst
                    saveTaxApi(loginModel.data?.bearer_access_token,
                        "gst",
                        enable_gst,
                        selectedStateID,
                        txtGstGstin.text.toString().trim(),
                        txtGstRegDate.text.toString().trim(),
                        periodicity_of_gst1,
                        0,
                        0,
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                    )
                }
            }

        }


    }
    fun saveTaxApi(token: String?,
                   type: String?,
                   enable_gst: Int?,
                   gst_state_id: String?,
                   gstin: String?,
                   registration_date: String?,
                   periodicity_of_gst1: String?,
                   enable_tcs: Int?,
                   enable_tds: Int?,
                   tan_number: String?,
                   tds_circle: String?,
                   tcs_collector_type: String?,
                   tcs_person_responsible: String?,
                   tcs_designation: String?,
                   tcs_contact_number: String?,
                   nature_of_goods: String?,
                   tds_deductor_type: String?,
                   tds_person_responsible: String?,
                   tds_designation: String?,
                   tds_contact_number: String?,
                   nature_of_payment: String?
    ){

        viewModel.saveTcsTdsDetailApi(
            token,
            type,
            enable_gst,
            gst_state_id,
            gstin,
            registration_date,
            periodicity_of_gst1,
            enable_tcs,
            enable_tds,
            tan_number,
            tds_circle,
            tcs_collector_type,
            tcs_person_responsible,
            tcs_designation,
            tcs_contact_number,
            nature_of_goods,
            tds_deductor_type,
            tds_person_responsible,
            tds_designation,
            tds_contact_number,
            nature_of_payment

        ).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (it.data?.status == true) {

                            Toast.makeText(
                                this,
                                it.data.message,
                                Toast.LENGTH_LONG
                            )
                                .show()

                            this.finish()

                        } else {
                            /*Toast.makeText(
                                this,
                                it.data?.errormessage?.message,
                                Toast.LENGTH_LONG
                            )
                                .show()*/
                            when (it.data!!.code == Constants.ErrorCode) {
                                true -> {
                                    Toast.makeText(
                                        this,
                                        it.data.errormessage?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                }
                                false -> {
                                    CommonUtils.somethingWentWrong(this)
                                }

                            }
                        }
                        CommonUtils.hideProgress()

                    }
                    Status.ERROR -> {
                        CommonUtils.hideProgress()
                        /*Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                            .show()*/
                    }
                    Status.LOADING -> {
                        CommonUtils.showProgress(this)
                    }
                }
            }
        })
    }



    fun openDatePicker(){

        //val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, monthOfYear)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            // Display Selected date in textbox
            txtGstRegDate.setText("" + String.format("%02d", dayOfMonth)   + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + year)

        }, year, month, day)

        dpd.datePicker.maxDate = Date().time
        dpd.show()
    }

    private fun getLoginModelFromPrefs() {
        prefs = PreferenceHelper.defaultPrefs(applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter
    }
    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                SaveTaxesViewModel::class.java
            )

        binding.setLifecycleOwner(this)
    }
    fun performValidation(): Boolean {
        when(enable_gst == 1){
            true -> {
                if(txtGstState.text.toString().isBlank() || selectedStateID?.isBlank() == true){
                    CommonUtils.showDialog(this, getString(R.string.select_state_msg))
                    txtGstState.requestFocus()
                    return false
                }else if(txtGstGstin.text.toString().isBlank()){
                    CommonUtils.showDialog(this, "Please Enter GSTIN")
                    txtGstGstin.requestFocus()
                    return false
                }else if(!CommonUtils.isValidGSTNo(txtGstGstin.text.toString())/*txtGSTIN.text?.length!! < 15*/){
                    txtGstGstin.error = getString(R.string.enter_valid_gstin_msg)
                    return false
                }else if(txtGstRegDate.text.toString().isBlank()){
                    CommonUtils.showDialog(this, "Please Enter Registration Date")
                    txtGstRegDate.requestFocus()
                    return false
                }
            }
            false -> {
                return true
            }
        }

        return true
    }
}