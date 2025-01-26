package com.goldbookapp.ui.activity.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.AlmostThereActivityBinding
import com.goldbookapp.model.CityModel
import com.goldbookapp.model.CountryModel
import com.goldbookapp.model.SignupModel
import com.goldbookapp.model.StateModel
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.viewmodel.AlmostThereViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.almost_there_activity.*


class AlmostThereActivity : AppCompatActivity() {

    private lateinit var viewModel: AlmostThereViewModel
    lateinit var popupMenu: PopupMenu
    lateinit var binding: AlmostThereActivityBinding

    lateinit var signupModel: SignupModel

    var countryList: List<CountryModel.Data.Country1948430004>? = null
    var stateList: List<StateModel.Data.State693361839>? = null
    var cityList: List<CityModel.Data.City1394158508>? = null

    var countryNameList: List<String>? = null
    var stateNameList: List<String>? = null
    var cityNameList: List<String>? = null

    lateinit var countryNameAdapter: ArrayAdapter<String>
    lateinit var stateNameAdapter: ArrayAdapter<String>
    lateinit var cityNameAdapter: ArrayAdapter<String>

    var selectedCountryID: String? = null
    var selectedStateID: String? = null
    var selectedCityID: String? = null
    var selectedTermBalanceID: String? = null
    var selectedTermBalanceName: String? = null
    var isGSTRegistered: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.almost_there_activity)

        setupViewModel()

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
            setupUIandListner()
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                AlmostThereViewModel::class.java
            )

    }

    private fun setupUIandListner() {

        val prefs = PreferenceHelper.defaultPrefs(this)
        signupModel = Gson().fromJson(
            prefs[Constants.PREF_COMPANY_REGISTER_KEY, ""],
            SignupModel::class.java
        ) //getter

        getCountry()


        txtBusinessName.doAfterTextChanged { tvBusinessNameInputLayout.error = null }
        autoCompleteBusinessLocation.doAfterTextChanged {
            tvBusinessLocation.error = null; selectedCountryID = "";
            autoCompleteState.setText(""); selectedStateID = "";
            autoCompleteCity.setText(""); selectedCityID = "";
            autoCompleteState.isEnabled = false
            autoCompleteState.isEnabled = false
        }
        autoCompleteState.doAfterTextChanged {
            tvState.error = null; selectedStateID = ""
            autoCompleteCity.setText(""); selectedCityID = "";
            autoCompleteCity.isEnabled = false
        }
        autoCompleteCity.doAfterTextChanged { tvCity.error = null; selectedCityID = "" }
        txtDefaultBalance.doAfterTextChanged { tvDefaultBalanceInputLayout.error = null }
        txtGSTIN.doAfterTextChanged { tvGSTINInputLayout.error = null }

        binding.btnCardDone.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {

                    companySetupAPI(
                        signupModel?.data?.user?.userInfo?.bearer_access_token,
                        binding.txtBusinessName.text.toString(),
                        selectedCountryID,
                        selectedStateID,
                        selectedCityID,
                        selectedTermBalanceID,
                        selectedTermBalanceName,
                        isGSTRegistered,
                        binding.txtGSTIN.text.toString(),
                        signupModel?.data?.user?.userInfo?.company_id
                    )
                }
            }
        }
        fillDefaultBalance()
        binding.txtDefaultBalance.clickWithDebounce {
            openDefaultTermPopup(binding.txtDefaultBalance)
        }

        radiogrpAlmost.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radiobtnGSTYes.id -> {
                    tvGSTINInputLayout.isEnabled = true; isGSTRegistered = "1"
                    tvGSTINInputLayout.visibility = View.VISIBLE
                }
                radiobtnGSTNo.id -> {
                    tvGSTINInputLayout.isEnabled = false; isGSTRegistered = "0"
                    tvGSTINInputLayout.visibility = View.GONE
                }
            }
        })
    }


    fun getCountry() {
        if (NetworkUtils.isConnected()) {
            viewModel.getCountry().observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                countryList = it.data.data?.country

                                countryNameList = countryList?.map { it.country_name }

                                countryNameAdapter = ArrayAdapter<String>(
                                    this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    countryNameList!!
                                )
                                binding.autoCompleteBusinessLocation.setAdapter(countryNameAdapter)
                                binding.autoCompleteBusinessLocation.threshold = 1

                                binding.autoCompleteBusinessLocation.setText("India")
                                selectedCountryID = countryList?.get(0)?.id
                                getState(selectedCountryID)
                                autoCompleteState.isEnabled = true

                                binding.autoCompleteBusinessLocation.setOnItemClickListener { adapterView, view, position, l
                                    ->
                                    val selected: String =
                                        adapterView.getItemAtPosition(position).toString()
                                    val pos: Int? = countryNameList?.indexOf(selected)

                                    selectedCountryID =
                                        pos?.let { it1 -> countryList?.get(it1)?.id }
                                    getState(selectedCountryID)
                                    autoCompleteState.isEnabled = true
                                }

                            } else {
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


                        }
                        Status.ERROR -> {

                        }
                        Status.LOADING -> {

                        }
                    }
                }
            })
        }

    }

    fun getState(countryID: String?) {

        viewModel.getState(countryID).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {

                        if (it.data?.status == true) {
                            stateList = it.data.data?.state

                            stateNameList = stateList?.map { it.name }

                            stateNameAdapter = ArrayAdapter<String>(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                stateNameList!!
                            )
                            binding.autoCompleteState.setAdapter(stateNameAdapter)
                            binding.autoCompleteState.threshold = 1

                            binding.autoCompleteState.setOnItemClickListener { adapterView, view, position, l
                                ->
                                val selected: String =
                                    adapterView.getItemAtPosition(position).toString()
                                val pos: Int? = stateNameList?.indexOf(selected)

                                selectedStateID = pos?.let { it1 -> stateList?.get(it1)?.id }
                                getCity(selectedStateID)
                                autoCompleteCity.isEnabled = true
                            }
                        } else {
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
                    }
                    Status.ERROR -> {

                    }
                    Status.LOADING -> {

                    }
                }
            }
        })
    }

    fun getCity(stateID: String?) {

        viewModel.getCity(stateID).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {

                        if (it.data?.status == true) {
                            cityList = it.data.data?.city

                            cityNameList = cityList?.map { it.name }

                            cityNameAdapter = ArrayAdapter<String>(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                cityNameList!!
                            )
                            binding.autoCompleteCity.setAdapter(cityNameAdapter)
                            binding.autoCompleteCity.threshold = 1

                            binding.autoCompleteCity.setOnItemClickListener { adapterView, view, position, l
                                ->
                                val selected: String =
                                    adapterView.getItemAtPosition(position).toString()
                                val pos: Int? = cityNameList?.indexOf(selected)

                                selectedCityID = pos?.let { it1 -> cityList?.get(it1)?.id }
                            }

                        } else {
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

                    }
                    Status.ERROR -> {

                    }
                    Status.LOADING -> {

                    }
                }
            }
        })
    }


    fun performValidation(): Boolean {

        if (txtBusinessName.text.toString().isBlank()) {
            tvBusinessNameInputLayout?.error = getString(R.string.enter_business_name_msg)
            return false
        } else if (autoCompleteBusinessLocation.text.toString()
                .isBlank() || selectedCountryID?.isBlank() == true
        ) {
            tvBusinessLocation?.error = getString(R.string.select_country_msg)
            return false
        } else if (autoCompleteState.text.toString()
                .isBlank() || selectedStateID?.isBlank() == true
        ) {
            tvState?.error = getString(R.string.select_state_territory_msg)
            return false
        } else if (autoCompleteCity.text.toString()
                .isBlank() || selectedCityID?.isBlank() == true
        ) {
            tvCity?.error = getString(R.string.select_city_msg)
            return false
        } else if (txtDefaultBalance.text.toString().isBlank()) {
            tvDefaultBalanceInputLayout?.error = getString(R.string.select_default_bal_msg)
            return false
        } else if (radiogrpAlmost.getCheckedRadioButtonId() == -1) { // no radio buttons are checked
            Toast.makeText(
                this,
                getString(R.string.select_gst_registered_dealer_msg),
                Toast.LENGTH_LONG
            ).show()
            return false
        } else if (radiobtnGSTYes.isChecked && txtGSTIN.text.toString().isBlank()) {
            tvGSTINInputLayout.error = getString(R.string.enter_gstin_msg)
            return false
        } else if (radiobtnGSTYes.isChecked && !CommonUtils.isValidGSTNo(txtGSTIN.text.toString())/*txtGSTIN.text?.length!! < 15*/) {
            tvGSTINInputLayout.error = getString(R.string.enter_valid_gstin_msg)
            return false
        }

        return true
    }

    private fun fillDefaultBalance() {
        popupMenu = PopupMenu(this, binding.txtDefaultBalance)
        popupMenu.menu.add(
            Menu.NONE,
            1,
            1,
            getString(R.string.debit_credit)
        )
        popupMenu.menu.add(
            Menu.NONE,
            2,
            2,
            getString(R.string.udhar_jama)
        )
        popupMenu.menu.add(
            Menu.NONE,
            3,
            3,
            getString(R.string.rec_pay)
        )
        popupMenu.menu.add(
            Menu.NONE,
            4,
            4,
            getString(R.string.len_den)
        )
        //add(groupId, itemId, order, title);
       // binding.txtDefaultBalance.setText(getString(R.string.debit_credit))
       // selectedTermBalanceID = popupMenu.menu.getItem(0).toString()
    }

    fun openDefaultTermPopup(view: View) {


        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when(item.itemId.toString()){
                // 1-> true //0-> false
                "1" -> {
                    selectedTermBalanceID = "1"
                    selectedTermBalanceName = "setting_debit_credit"
                }
                "2" -> {
                    selectedTermBalanceID = "2"
                    selectedTermBalanceName = "setting_udhar_jama"
                }
                "3" -> {
                    selectedTermBalanceID = "3"
                    selectedTermBalanceName = "setting_receivable_payable"

                }
                "4" -> {
                    selectedTermBalanceID = "4"
                    selectedTermBalanceName = "setting_len_den"

                }
            }
            binding.txtDefaultBalance.setText(item.title)
           // selectedTermBalanceID = item.itemId.toString()
            true
        })

        popupMenu.show()
    }

    fun companySetupAPI(
        access_token: String?,
        company_name: String?,
        business_location: String?,
        state_id: String?,
        city_id: String?,
        term_balance: String?,
        default_term: String?,
        gst_register: String?,
        gst_tin_number: String?,
        company_id: String?
    ) {

        viewModel.companySetup(
            access_token,
            company_name,
            business_location,
            state_id,
            city_id,
            term_balance,
            default_term,
            gst_register,
            gst_tin_number,
            company_id
        ).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {

                        if (it.data?.status == true) {

                            loginAPI(
                                signupModel.data?.user?.userInfo?.username,
                                signupModel.data?.user?.userInfo?.password
                            )

                            Toast.makeText(
                                this,
                                it.data?.message,
                                Toast.LENGTH_LONG
                            )
                                .show()
                        } else {
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

                    }
                    Status.LOADING -> {
                        CommonUtils.showProgress(this)
                    }
                }
            }
        })
    }

    fun loginAPI(email: String?, password: String?) {
        if (NetworkUtils.isConnected()) {
            viewModel.getLoginData(email, password)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                CommonUtils.hideProgress()

                                if (it.data?.status == true) {

                                    it.data.data?.bearer_access_token =
                                        "Bearer " + it.data.data?.access_token

                                    val prefs = PreferenceHelper.defaultPrefs(this)
                                    prefs[Constants.PREF_LOGIN_DETAIL_KEY] =
                                        Gson().toJson(it.data) //setter

                                    startActivity(
                                        Intent(this, MainActivity::class.java).setFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        )
                                    )

                                } else {

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
                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()
                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(this)

                            }
                        }
                    }
                })

        }
    }

    override fun onBackPressed() {

        showWarningDialog(this, getString(R.string.skip_registration_validation_msg))
    }

    fun showWarningDialog(context: Context, message: String) {
        MaterialDialog(context).show {
            title(R.string.app_name)
            message(text = message)
            cancelable(false)
            positiveButton(R.string.yes) {
                startActivity(
                    Intent(
                        context,
                        LoginActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
            negativeButton(R.string.no) {
                dismiss()
            }
        }
    }
}