package com.goldbookapp.ui.activity.customer

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.user.BackToLoginActivity
import com.goldbookapp.ui.activity.viewmodel.AddressDetailsViewModel
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
import kotlinx.android.synthetic.main.address_detail_activity.*
import kotlinx.android.synthetic.main.recover_account_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class CustSuppAddressDetailsActivity : AppCompatActivity() {

    private lateinit var viewModel: AddressDetailsViewModel

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
    var isFromEdit: Boolean = false

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
        tvTitle.setText(R.string.address_detail)

        isFromEdit = false

        try {
            if (prefs.contains(Constants.PREF_BILLING_ADDRESS_KEY) && prefs.contains(Constants.PREF_SHIPPING_ADDRESS_KEY)) {
                var billingAddress: BillingAddressModel = Gson().fromJson(
                    prefs[Constants.PREF_BILLING_ADDRESS_KEY, ""],
                    BillingAddressModel::class.java
                )

                if (billingAddress.country_id.isNullOrBlank()) isFromEdit = false
                else isFromEdit = true
                txtOfficeAddres.setText(billingAddress.location)
                txtAreaAddress.setText(billingAddress.area)
                txtLandmarkAddress.setText(billingAddress.landmark)
                txtCountryAddress.setText(billingAddress.country_name)
                selectedCountryID = billingAddress.country_id
                txtStateAddress.setText(billingAddress.state_name)
                selectedStateID = billingAddress.state_id
                txtCityAddress.setText(billingAddress.city_name)
                selectedCityID = billingAddress.city_id
                txtPinAddress.setText(billingAddress.pincode)
                txtLandlineAddress.setText(billingAddress.mobile_no)
                txtAdditionalAddress.setText(billingAddress.secondary_no)
                txtFaxAddress.setText(billingAddress.fax_no)


                when (billingAddress.is_shipping) {
                    "1" -> {
                        linearShippingAddress.visibility = View.GONE
                        checkSameAsBillingAddress.isChecked = true
                    }
                    "0" -> {
                        linearShippingAddress.visibility = View.VISIBLE
                        checkSameAsBillingAddress.isChecked = false
                    }
                }

                // Shipping
                val shipppingAddress: ShippingAddressModel = Gson().fromJson(
                    prefs[Constants.PREF_SHIPPING_ADDRESS_KEY, ""],
                    ShippingAddressModel::class.java
                )
                txtOfficeAddresShipping.setText(shipppingAddress.location)
                txtAreaAddressShipping.setText(shipppingAddress.area)
                txtLandmarkAddressShipping.setText(shipppingAddress.landmark)
                txtCountryAddressShipping.setText(shipppingAddress.country_name)
                selectedCountryIDShipping = shipppingAddress.country_id
                txtStateAddressShipping.setText(shipppingAddress.state_name)
                selectedStateIDShipping = shipppingAddress.state_id
                txtCityAddressShipping.setText(shipppingAddress.city_name)
                selectedCityIDShipping = shipppingAddress.city_id
                txtPinAddressShipping.setText(shipppingAddress.pincode)
                txtLandlineAddressShipping.setText(shipppingAddress.mobile_no)
                txtAdditionalAddressShipping.setText(shipppingAddress.secondary_no)
                txtFaxAddressShipping.setText(shipppingAddress.fax_no)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Billing
        txtCountryAddress.doAfterTextChanged {

            selectedCountryID = "";
            txtStateAddress.setText(""); selectedStateID = "";
            txtCityAddress.setText(""); selectedCityID = "";
            tvStateAddress.isEnabled = false
            tvCityAddress.isEnabled = false
        }
        txtStateAddress.doAfterTextChanged {

            selectedStateID = ""
            txtCityAddress.setText(""); selectedCityID = "";
            tvCityAddress.isEnabled = false
        }
        txtCityAddress.doAfterTextChanged {

            selectedCityID = ""
        }

        // Shipping
        txtCountryAddressShipping.doAfterTextChanged {

            selectedCountryIDShipping = "";
            txtStateAddressShipping.setText(""); selectedStateIDShipping = "";
            txtCityAddressShipping.setText(""); selectedCityIDShipping = "";
            tvStateAddressShipping.isEnabled = false
            tvCityAddressShipping.isEnabled = false
        }
        txtStateAddressShipping.doAfterTextChanged {

            selectedStateIDShipping = ""
            txtCityAddressShipping.setText(""); selectedCityIDShipping = "";
            tvCityAddressShipping.isEnabled = false
        }
        txtCityAddressShipping.doAfterTextChanged {

            selectedCityIDShipping = ""
        }

        btnReset?.clickWithDebounce {

            startActivity(Intent(this, BackToLoginActivity::class.java))
        }

        imgLeft?.clickWithDebounce {

            onBackPressed()
        }

        btnSaveAdd_AddressDetail?.clickWithDebounce {
            if (performValidation()) {
                //tvRight.isEnabled = false
                saveBillingShippingAddressModel()
            }
        }
        if (!isFromEdit) {
            checkSameAsBillingAddress.isChecked = true
            linearShippingAddress.visibility = View.GONE
        }


        checkSameAsBillingAddress?.setOnCheckedChangeListener { compoundButton,
                                                                isChecked ->
            if (isChecked) {
                linearShippingAddress.visibility = View.GONE
            } else {
                linearShippingAddress.visibility = View.VISIBLE
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
                                binding.txtCountryAddress.setAdapter(countryNameAdapter)
                                binding.txtCountryAddress.threshold = 1

                                if (!isFromEdit) {
                                    binding.txtCountryAddress.setText("India")
                                    binding.txtCountryAddressShipping.setText("India")
                                    selectedCountryID = countryList?.get(0)?.id
                                    selectedCountryIDShipping = countryList?.get(0)?.id
                                    getState(selectedCountryID, true)
                                    getState(selectedCountryID, false)
                                    tvStateAddress.isEnabled = true
                                    tvStateAddressShipping.isEnabled = true
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
                                if (isFromEdit) {
                                    getState(selectedCountryID, true)
                                    tvStateAddress.isEnabled = true
                                }


                                // Shipping
                                binding.txtCountryAddressShipping.setAdapter(countryNameAdapter)
                                binding.txtCountryAddressShipping.threshold = 1

                                binding.txtCountryAddressShipping.setOnItemClickListener { adapterView, view, position, l
                                    ->
                                    val selected: String =
                                        adapterView.getItemAtPosition(position).toString()
                                    val pos: Int? = countryNameList?.indexOf(selected)

                                    selectedCountryIDShipping =
                                        pos?.let { it1 -> countryList?.get(it1)?.id }
                                    getState(selectedCountryIDShipping, false)
                                    tvStateAddressShipping.isEnabled = true
                                }
                                if (!isFromEdit) {
                                    //nothing to do
                                } else {
                                    getState(selectedCountryIDShipping, false)
                                    tvStateAddressShipping.isEnabled = true
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

                                getCity(selectedStateID, true)
                                tvCityAddress.isEnabled = true


                            } else {
                                binding.txtStateAddressShipping.setAdapter(stateNameAdapter)
                                binding.txtStateAddressShipping.threshold = 1

                                binding.txtStateAddressShipping.setOnItemClickListener { adapterView, view, position, l
                                    ->
                                    val selected: String =
                                        adapterView.getItemAtPosition(position).toString()
                                    val pos: Int? = stateNameList?.indexOf(selected)

                                    selectedStateIDShipping =
                                        pos?.let { it1 -> stateList?.get(it1)?.id }
                                    getCity(selectedStateIDShipping, false)
                                    tvCityAddressShipping.isEnabled = true
                                }

                                getCity(selectedStateIDShipping, false)
                                tvCityAddressShipping.isEnabled = true

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
                            } else {
                                binding.txtCityAddressShipping.setAdapter(cityNameAdapter)
                                binding.txtCityAddressShipping.threshold = 1

                                binding.txtCityAddressShipping.setOnItemClickListener { adapterView, view, position, l
                                    ->
                                    val selected: String =
                                        adapterView.getItemAtPosition(position).toString()
                                    val pos: Int? = cityNameListShipping?.indexOf(selected)

                                    selectedCityIDShipping =
                                        pos?.let { it1 -> cityListShipping?.get(it1)?.id }
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

        if (txtCountryAddress.text.toString().isBlank() || selectedCountryID?.isBlank()!!
        ) {
            CommonUtils.showDialog(
                this,
                getString(R.string.select_country_msg)/*"Please Select Country"*/
            )
            txtCountryAddress.requestFocus()
            return false
        } else if (txtStateAddress.text.toString().isBlank() || selectedStateID?.isBlank()!!) {
            CommonUtils.showDialog(this, getString(R.string.select_state_msg))
            txtStateAddress.requestFocus()
            return false
        } else if (!checkSameAsBillingAddress.isChecked) {
            if (txtCountryAddressShipping.text.toString()
                    .isBlank() || selectedCountryID?.isBlank() == true
            ) {
                CommonUtils.showDialog(
                    this,
                    getString(R.string.select_country_msg)/*"Please Select Country"*/
                )
                txtCountryAddressShipping.requestFocus()
                return false
            } else if (txtStateAddressShipping.text.toString()
                    .isBlank() || selectedStateID?.isBlank() == true
            ) {
                CommonUtils.showDialog(
                    this,
                    getString(R.string.select_state_msg)/*"Please Select State"*/
                )
                txtStateAddressShipping.requestFocus()
                return false
            }
        }

        return true
    }

    fun saveBillingShippingAddressModel() {


        val childBillingModel = BillingAddressModel(
            "", "",
            txtOfficeAddres.text.toString().trim(),
            txtAreaAddress.text.toString().trim(),
            txtLandmarkAddress.text.toString().trim(),
            selectedCountryID,
            txtCountryAddress.text.toString().trim(),
            selectedStateID,
            txtStateAddress.text.toString().trim(),
            selectedCityID,
            txtCityAddress.text.toString().trim(),
            txtPinAddress.text.toString().trim(),
            txtLandlineAddress.text.toString().trim(),
            txtAdditionalAddress.text.toString().trim(),
            txtFaxAddress.text.toString().trim(),
            when {
                checkSameAsBillingAddress.isChecked -> "1"
                else -> "0"
            }
        )


        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_BILLING_ADDRESS_KEY] = Gson().toJson(childBillingModel) //setter


        if (checkSameAsBillingAddress.isChecked) {

            val childShippingModel = ShippingAddressModel(
                "", "",
                childBillingModel.location,
                childBillingModel.area,
                childBillingModel.landmark,
                childBillingModel.country_id,
                childBillingModel.country_name,
                childBillingModel.state_id,
                childBillingModel.state_name,
                childBillingModel.city_id,
                childBillingModel.city_name,
                childBillingModel.pincode,
                childBillingModel.mobile_no,
                childBillingModel.secondary_no,
                childBillingModel.fax_no
            )

            val prefs = PreferenceHelper.defaultPrefs(this)
            prefs[Constants.PREF_SHIPPING_ADDRESS_KEY] = Gson().toJson(childShippingModel) //setter


        } else {

            val childShippingModel = ShippingAddressModel(
                "", "",
                txtOfficeAddresShipping.text.toString().trim(),
                txtAreaAddressShipping.text.toString().trim(),
                txtLandmarkAddressShipping.text.toString().trim(),
                selectedCountryIDShipping,
                txtCountryAddressShipping.text.toString().trim(),
                selectedStateIDShipping,
                txtStateAddressShipping.text.toString().trim(),
                selectedCityIDShipping,
                txtCityAddressShipping.text.toString().trim(),
                txtPinAddressShipping.text.toString().trim(),
                txtLandlineAddressShipping.text.toString().trim(),
                txtAdditionalAddressShipping.text.toString().trim(),
                txtFaxAddressShipping.text.toString().trim()
            )

            val prefs = PreferenceHelper.defaultPrefs(this)
            prefs[Constants.PREF_SHIPPING_ADDRESS_KEY] = Gson().toJson(childShippingModel) //setter

        }

        onBackPressed()
    }
}