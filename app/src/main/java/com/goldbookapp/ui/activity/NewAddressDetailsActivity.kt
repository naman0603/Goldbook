package com.goldbookapp.ui.activity

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
import com.goldbookapp.databinding.AddressDetailActivityBinding
import com.goldbookapp.model.CityModel
import com.goldbookapp.model.CountryModel
import com.goldbookapp.model.ShippingOrOtherAddressModel
import com.goldbookapp.model.StateModel
import com.goldbookapp.ui.activity.viewmodel.AddressDetailsViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.address_detail_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class NewAddressDetailsActivity : AppCompatActivity() {

    private lateinit var viewModel: AddressDetailsViewModel
    private var type: String? = "contact" //contact/address
    private var sub_type: String? = "shipping" //shipping/other if type is address.
    lateinit var custAddressModel: ShippingOrOtherAddressModel
    lateinit var suppAddressModel: ShippingOrOtherAddressModel
    private var edited_custaddress_pos: Int = 0
    private var edited_suppaddress_pos: Int = 0
    lateinit var binding: AddressDetailActivityBinding

    var countryList: List<CountryModel.Data.Country1948430004>? = null
    var stateList: List<StateModel.Data.State693361839>? = null
    var cityList: List<CityModel.Data.City1394158508>? = null
    var cityListShipping: List<CityModel.Data.City1394158508>? = null

    var countryNameList: List<String>? = null
    var stateNameList: List<String>? = null
    var cityNameList: List<String>? = null
    var cityNameListShipping: List<String>? = null

    lateinit var countryNameAdapter: ArrayAdapter<String>
    lateinit var stateNameAdapter: ArrayAdapter<String>
    lateinit var cityNameAdapter: ArrayAdapter<String>

    var selectedCountryID: String? = null
    var selectedStateID: String? = null
    var selectedCityID: String? = null

    var selectedCountryIDShipping: String? = null
    var selectedStateIDShipping: String? = null
    var selectedCityIDShipping: String? = null
    var isFromCustEdit: Boolean = false // true -> edit cust false -> edit supp
    var isFromCustAdd: Boolean = false // true -> new cust false -> new supp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.address_detail_activity)

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
                AddressDetailsViewModel::class.java
            )

    }

    private fun setupUIandListner() {
        val prefs = PreferenceHelper.defaultPrefs(this)

        getCountry()

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.addtional_information)
        tvRight.setText(R.string.save)

        if (intent.extras != null && intent.extras?.containsKey(Constants.isFromNewCustAddress)!!) {
            isFromCustAdd = intent.getBooleanExtra(Constants.isFromNewCustAddress, true)

            when (isFromCustAdd) {
                true -> {
                    //is from new cust addinfo
                }
                false -> {
                    //is from new supp addinfo
                }
            }

        } else {
            // edit cust / supp
            if (intent.extras?.containsKey(Constants.isFromEditCustAddress)!!) {
                isFromCustEdit = intent.getBooleanExtra(Constants.isFromEditCustAddress, true)
                when (isFromCustEdit) {
                    // cust address edit
                    true -> {
                        val custAddressStr: String? =
                            intent.getStringExtra(Constants.PREF_ADD_EDIT_CUST_SHIPPING_OTHER_KEY)
                        custAddressModel = Gson().fromJson(
                            custAddressStr,
                            ShippingOrOtherAddressModel::class.java
                        )
                        edited_custaddress_pos = intent.getIntExtra(Constants.EDIT_CUST_POS_KEY, 0)
                        setCustAddressFiedls(custAddressModel)
                    }
                    // supp address edit
                    false -> {
                        val suppAddressStr: String? =
                            intent.getStringExtra(Constants.PREF_ADD_EDIT_SUPP_SHIPPING_OTHER_KEY)
                        suppAddressModel = Gson().fromJson(
                            suppAddressStr,
                            ShippingOrOtherAddressModel::class.java
                        )
                        edited_suppaddress_pos = intent.getIntExtra(Constants.EDIT_SUPP_POS_KEY, 0)
                        setSuppAddressFiedls(suppAddressModel)

                    }
                }
            }

        }


        try {
            if (prefs.contains(Constants.PREF_ADDITIONAL_iNFO_ADDRESS_KEY)) {
                var allInOneAddress: ShippingOrOtherAddressModel = Gson().fromJson(
                    prefs[Constants.PREF_ADDITIONAL_iNFO_ADDRESS_KEY, ""],
                    ShippingOrOtherAddressModel::class.java
                )
                when (allInOneAddress.type) {
                    "contact" -> {
                        type = "contact"
                        sub_type = ""
                    }
                    "address" -> {
                        when (allInOneAddress.sub_type) {
                            "shipping" -> {
                                type = "address"
                                sub_type = "shipping"
                            }
                            "other" -> {
                                type = "address"
                                sub_type = "other"
                            }
                        }
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Billing
        txtCountryAddress.doAfterTextChanged {
            //tvBusinessLocation.error = null; selectedCountryID = "";
            selectedCountryID = "";
            txtStateAddress.setText(""); selectedStateID = "";
            txtCityAddress.setText(""); selectedCityID = "";
            tvStateAddress.isEnabled = false
            tvCityAddress.isEnabled = false
        }
        txtStateAddress.doAfterTextChanged {
            //tvState.error = null; selectedStateID = ""
            selectedStateID = ""
            txtCityAddress.setText(""); selectedCityID = "";
            tvCityAddress.isEnabled = false
        }
        txtCityAddress.doAfterTextChanged {
            //tvCity.error = null; selectedCityID = ""
            selectedCityID = ""
        }


        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        tvRight?.clickWithDebounce {
            if (performValidation()) {
                //tvRight.isEnabled = false
                saveAllAddressModel()
            }
        }
        if (!isFromCustEdit) {

        }

    }

    private fun setSuppAddressFiedls(suppAddressModel: ShippingOrOtherAddressModel?) {

    }

    private fun setCustAddressFiedls(custAddressModel: ShippingOrOtherAddressModel) {
        TODO("Not yet implemented")
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
                                binding.txtCountryAddress.setAdapter(countryNameAdapter)
                                binding.txtCountryAddress.threshold = 1

                                if (!isFromCustEdit) {
                                    binding.txtCountryAddress.setText("India")

                                    selectedCountryID = countryList?.get(0)?.id
                                    selectedCountryIDShipping = countryList?.get(0)?.id
                                    getState(selectedCountryID, true)
                                    getState(selectedCountryID, false)
                                    tvStateAddress.isEnabled = true
                                }


                                binding.txtCountryAddress.setOnItemClickListener { adapterView, view, position, l
                                    ->
                                    val selected: String =
                                        adapterView.getItemAtPosition(position).toString()
                                    val pos: Int? = countryNameList?.indexOf(selected)

                                    selectedCountryID =
                                        pos?.let { it1 -> countryList?.get(it1)?.id }
                                    getState(selectedCountryID, true)
                                    tvStateAddress.isEnabled = true
                                }
                                if (isFromCustEdit) {
                                    getState(selectedCountryID, true)
                                    tvStateAddress.isEnabled = true
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

    }

    fun getState(countryID: String?, isBillingAddress: Boolean) {
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

                            if (isBillingAddress) {
                                binding.txtStateAddress.setAdapter(stateNameAdapter)
                                binding.txtStateAddress.threshold = 1

                                binding.txtStateAddress.setOnItemClickListener { adapterView, view, position, l
                                    ->
                                    val selected: String =
                                        adapterView.getItemAtPosition(position).toString()
                                    val pos: Int? = stateNameList?.indexOf(selected)

                                    selectedStateID = pos?.let { it1 -> stateList?.get(it1)?.id }
                                    getCity(selectedStateID, true)
                                    tvCityAddress.isEnabled = true
                                }

                                // nothing to do
                                getCity(selectedStateID, true)
                                tvCityAddress.isEnabled = true
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

    fun getCity(stateID: String?, isBillingAddress: Boolean) {

        viewModel.getCity(stateID).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {

                        if (it.data?.status == true) {
                            when (isBillingAddress) {
                                true -> {
                                    cityList = it.data.data?.city

                                    cityNameList = cityList?.map { it.name }

                                    cityNameAdapter = ArrayAdapter<String>(
                                        this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        cityNameList!!
                                    )
                                }
                                false -> {
                                    cityListShipping = it.data.data?.city

                                    cityNameListShipping = cityListShipping?.map { it.name }

                                    cityNameAdapter = ArrayAdapter<String>(
                                        this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        cityNameListShipping!!
                                    )
                                }
                            }

                            if (isBillingAddress) {
                                binding.txtCityAddress.setAdapter(cityNameAdapter)
                                binding.txtCityAddress.threshold = 1

                                binding.txtCityAddress.setOnItemClickListener { adapterView, view, position, l
                                    ->
                                    val selected: String =
                                        adapterView.getItemAtPosition(position).toString()
                                    val pos: Int? = cityNameList?.indexOf(selected)

                                    selectedCityID = pos?.let { it1 -> cityList?.get(it1)?.id }
                                }
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

        if (txtCountryAddress.text.toString().isBlank() || selectedCountryID?.isBlank() == true) {
            CommonUtils.showDialog(
                this,
                getString(R.string.select_country_msg)/*"Please Select Country"*/
            )
            txtCountryAddress.requestFocus()
            return false
        } else if (txtStateAddress.text.toString()
                .isBlank() || selectedStateID?.isBlank() == true
        ) {
            CommonUtils.showDialog(this, getString(R.string.select_state_msg))
            txtStateAddress.requestFocus()
            return false
        }

        return true
    }

    fun saveAllAddressModel() {
        onBackPressed()
    }
}