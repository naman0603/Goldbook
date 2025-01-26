package com.goldbookapp.ui.activity.organization

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.CompanyAddressDetailActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.CompanyAddressDetailsViewModel
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
import kotlinx.android.synthetic.main.company_address_detail_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class CompanyAddressDetailsActivity : AppCompatActivity() {

    private lateinit var viewModel: CompanyAddressDetailsViewModel

    lateinit var binding: CompanyAddressDetailActivityBinding

    var countryList: List<CountryModel.Data.Country1948430004>? = null
    var stateList: List<StateModel.Data.State693361839>? = null
    var cityList: List<CityModel.Data.City1394158508>? = null

    var countryNameList: List<String>? = null
    var stateNameList: List<String>? = null
    var cityNameList: List<String>? = null

    var isfromBranch: Boolean = false

    lateinit var countryNameAdapter: ArrayAdapter<String>
    lateinit var stateNameAdapter: ArrayAdapter<String>
    lateinit var cityNameAdapter: ArrayAdapter<String>

    var selectedCountryID: String? = null
    var selectedStateID: String? = null
    var selectedCityID: String? = null
    var isFromEdit: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.company_address_detail_activity)


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
                CompanyAddressDetailsViewModel::class.java
            )

    }

    private fun setupUIandListner() {
        val prefs = PreferenceHelper.defaultPrefs(this)

        getCountry()

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.address_detail)

        isFromEdit = false

        if (intent.extras != null && intent.extras!!.containsKey("isFromBranch")) {
            isfromBranch = (intent.getBooleanExtra("isFromBranch", false))
            if (prefs.contains(Constants.PREF_BRANCH_ADDRESS_KEY)) {
                var branchAddress: BranchAddressModel = Gson().fromJson(
                    prefs[Constants.PREF_BRANCH_ADDRESS_KEY, ""],
                    BranchAddressModel::class.java
                )
                txtOfficeCompAddress.setText(branchAddress.branch_address)
                txtAreaCompAddress.setText(branchAddress.area)
                txtLandmarkCompAddress.setText(branchAddress.landmark)
                txtCountryCompAddress.setText(branchAddress.country_name)
                selectedCountryID = branchAddress.country_id
                txtStateCompAddress.setText(branchAddress.state_name)
                selectedStateID = branchAddress.state_id
                txtCityCompAddress.setText(branchAddress.city_name)
                selectedCityID = branchAddress.city_id
                txtPincodeCompAddress.setText(branchAddress.pincode)
                isFromEdit = true
            }
        } else {
            try {
                if (prefs.contains(Constants.PREF_COMPANY_ADDRESS_KEY)) {
                    var companyAddress: CompanyAddressModel = Gson().fromJson(
                        prefs[Constants.PREF_COMPANY_ADDRESS_KEY, ""],
                        CompanyAddressModel::class.java
                    )
                    txtOfficeCompAddress.setText(companyAddress.location)
                    txtAreaCompAddress.setText(companyAddress.area)
                    txtLandmarkCompAddress.setText(companyAddress.landmark)
                    txtCountryCompAddress.setText(companyAddress.country_name)
                    selectedCountryID = companyAddress.country_id
                    txtStateCompAddress.setText(companyAddress.state_name)
                    selectedStateID = companyAddress.state_id
                    txtCityCompAddress.setText(companyAddress.city_name)
                    selectedCityID = companyAddress.city_id
                    txtPincodeCompAddress.setText(companyAddress.pincode)

                    isFromEdit = true

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }



        txtCountryCompAddress.doAfterTextChanged {
            //tvBusinessLocation.error = null; selectedCountryID = "";
            selectedCountryID = "";
            txtStateCompAddress.setText(""); selectedStateID = "";
            txtCityCompAddress.setText(""); selectedCityID = "";
            txtStateCompAddress.isEnabled = false
            txtCityCompAddress.isEnabled = false
        }
        txtStateCompAddress.doAfterTextChanged {
            //tvState.error = null; selectedStateID = ""
            selectedStateID = ""
            txtCityCompAddress.setText(""); selectedCityID = "";
            txtCityCompAddress.isEnabled = false
        }
        txtCityCompAddress.doAfterTextChanged {
            //tvCity.error = null; selectedCityID = ""
            selectedCityID = ""
        }


        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        btnSaveAddressCompany?.clickWithDebounce {
            if (isfromBranch) {
                if (performValidation())
                    saveBranchAddressModel()
            } else {
                if (performValidationCompany())
                    saveCompanyAddressModel()
            }

        }
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
                                binding.txtCountryCompAddress.setAdapter(countryNameAdapter)
                                binding.txtCountryCompAddress.threshold = 1


                                when (isFromEdit) {
                                    true -> {
                                        getState(selectedCountryID)
                                        txtStateCompAddress.isEnabled = true
                                    }
                                    else -> {
                                        binding.txtCountryCompAddress.setText("India")
                                        selectedCountryID = countryList?.get(0)?.id
                                        getState(selectedCountryID)
                                        txtStateCompAddress.isEnabled = true
                                    }
                                }


                                binding.txtCountryCompAddress.setOnItemClickListener { adapterView, view, position, l
                                    ->
                                    val selected: String =
                                        adapterView.getItemAtPosition(position).toString()
                                    val pos: Int? = countryNameList?.indexOf(selected)

                                    selectedCountryID =
                                        pos?.let { it1 -> countryList?.get(it1)?.id }
                                    getState(selectedCountryID)
                                    txtStateCompAddress.isEnabled = true
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


                            binding.txtStateCompAddress.setAdapter(stateNameAdapter)
                            binding.txtStateCompAddress.threshold = 1

                            binding.txtStateCompAddress.setOnItemClickListener { adapterView, view, position, l
                                ->
                                val selected: String =
                                    adapterView.getItemAtPosition(position).toString()
                                val pos: Int? = stateNameList?.indexOf(selected)

                                selectedStateID = pos?.let { it1 -> stateList?.get(it1)?.id }
                                getCity(selectedStateID)
                                txtCityCompAddress.isEnabled = true
                            }

                            when (selectedStateID.isNullOrBlank()) {
                                true -> {
                                    // nothing to do when null state
                                }
                                else -> {
                                    getCity(selectedStateID)
                                    txtCityCompAddress.isEnabled = true
                                }
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


                            binding.txtCityCompAddress.setAdapter(cityNameAdapter)
                            binding.txtCityCompAddress.threshold = 1

                            binding.txtCityCompAddress.setOnItemClickListener { adapterView, view, position, l
                                ->
                                val selected: String =
                                    adapterView.getItemAtPosition(position).toString()
                                val pos: Int? = cityNameList?.indexOf(selected)

                                selectedCityID = pos?.let { it1 -> cityList?.get(it1)?.id }
                            }

                        } else {
                            Toast.makeText(
                                this,
                                it.data?.errormessage?.message,
                                Toast.LENGTH_LONG
                            )
                                .show()
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

        if (txtCountryCompAddress.text.toString()
                .isBlank() || selectedCountryID?.isBlank() == true
        ) {
            CommonUtils.showDialog(
                this,
                getString(R.string.select_country_msg)/*"Please Select Country"*/
            )
            txtCountryCompAddress.requestFocus()
            return false
        } else if (txtStateCompAddress.text.toString()
                .isBlank() || selectedStateID?.isBlank() == true
        ) {
            CommonUtils.showDialog(
                this,
                getString(R.string.select_state_msg)/*"Please Select State"*/
            )
            txtStateCompAddress.requestFocus()
            return false
        } else if (txtCityCompAddress.text.toString()
                .isBlank() || selectedCityID?.isBlank() == true
        ) {
            CommonUtils.showDialog(this, getString(R.string.select_city_msg))
            txtCityCompAddress.requestFocus()
            return false
        }


        return true
    }

    fun performValidationCompany(): Boolean {

        if (txtOfficeCompAddress.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_office_building_apartment_msg))
            txtOfficeCompAddress.requestFocus()
            return false
        } else if (txtAreaCompAddress.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_area_colony_street_sector_msg))
            txtAreaCompAddress.requestFocus()
            return false
        } else if (txtLandmarkCompAddress.text.toString().isBlank()) {

            CommonUtils.showDialog(this, getString(R.string.enter_landmark_msg))
            txtLandmarkCompAddress.requestFocus()
            return false
        } else if (txtCountryCompAddress.text.toString()
                .isBlank() || selectedCountryID?.isBlank() == true
        ) {
            CommonUtils.showDialog(this, getString(R.string.select_country_msg))
            txtCountryCompAddress.requestFocus()
            return false
        } else if (txtStateCompAddress.text.toString()
                .isBlank() || selectedStateID?.isBlank() == true
        ) {
            CommonUtils.showDialog(this, getString(R.string.select_state_msg))
            txtStateCompAddress.requestFocus()
            return false
        } else if (txtCityCompAddress.text.toString()
                .isBlank() || selectedCityID?.isBlank() == true
        ) {
            CommonUtils.showDialog(this, getString(R.string.select_city_msg))
            txtCityCompAddress.requestFocus()
            return false
        } else if (txtPincodeCompAddress.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_pincode_msg))
            txtPincodeCompAddress.requestFocus()
            return false
        } else if (txtPincodeCompAddress.text?.length!! < 6) {
            CommonUtils.showDialog(this, getString(R.string.enter_valid_pincode_msg))
            txtPincodeCompAddress.requestFocus()
            return false
        }


        return true
    }

    fun saveBranchAddressModel() {


        val childBranchModel = BranchAddressModel(
            txtOfficeCompAddress.text.toString().trim(),
            txtAreaCompAddress.text.toString().trim(),
            txtLandmarkCompAddress.text.toString().trim(),
            selectedCountryID,
            txtCountryCompAddress.text.toString().trim(),
            selectedStateID,
            txtStateCompAddress.text.toString().trim(),
            selectedCityID,
            txtCityCompAddress.text.toString().trim(),
            txtPincodeCompAddress.text.toString().trim()
        )

        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_BRANCH_ADDRESS_KEY] = Gson().toJson(childBranchModel) //setter

        onBackPressed()
    }

    fun saveCompanyAddressModel() {


        val childCompanyModel = CompanyAddressModel(
            txtOfficeCompAddress.text.toString().trim(),
            txtAreaCompAddress.text.toString().trim(),
            txtLandmarkCompAddress.text.toString().trim(),
            selectedCountryID,
            txtCountryCompAddress.text.toString().trim(),
            selectedStateID,
            txtStateCompAddress.text.toString().trim(),
            selectedCityID,
            txtCityCompAddress.text.toString().trim(),
            txtPincodeCompAddress.text.toString().trim()
        )

        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_COMPANY_ADDRESS_KEY] = Gson().toJson(childCompanyModel) //setter

        onBackPressed()
    }
}