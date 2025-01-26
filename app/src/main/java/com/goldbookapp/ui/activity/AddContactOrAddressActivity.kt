package com.goldbookapp.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityAddContactOrAddressBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.ShippingOrOtherAddressModel
import com.goldbookapp.ui.activity.settings.TCSActivity
import com.goldbookapp.ui.activity.viewmodel.AddContactOrAddressViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.AddtionalInfoContOtherShipAdapter
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_add_contact_or_address.*
import kotlinx.android.synthetic.main.activity_taxes.*
import kotlinx.android.synthetic.main.activity_tcs_tds.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class AddContactOrAddressActivity : AppCompatActivity() {
    private lateinit var viewModel: AddContactOrAddressViewModel
    private var isFromNewCustAddress: Boolean = true
    private var isFromEditCust: Boolean = true
    var list_Cust_Cont_Ship_Other: ArrayList<ShippingOrOtherAddressModel>? = null
    var list_Supp_Cont_Ship_Other: ArrayList<ShippingOrOtherAddressModel>? = null
    lateinit var binding: ActivityAddContactOrAddressBinding
    lateinit var prefs: SharedPreferences
    private lateinit var addEditContOrAddAdapter: AddtionalInfoContOtherShipAdapter

    lateinit var loginModel: LoginModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_contact_or_address)
        setupViewModel()
        setupUIandListner()

    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                AddContactOrAddressViewModel::class.java
            )

        binding.setLifecycleOwner(this)
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
            when (isFromNewCustAddress) {
                true -> {
                    // new/edit customer
                    setCustContactorAddressData()
                }
                false -> {
                    // new/edit supplier
                    setSuppContactorAddressData()
                }
            }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun setupUIandListner() {
        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.addtional_information)
        tvRight.setText(R.string.save)
        getLoginModelFromPrefs()


        if (intent.extras != null && intent.extras?.containsKey(Constants.isFromNewEditCustAddress)!!) {
            isFromNewCustAddress = intent.getBooleanExtra(Constants.isFromNewEditCustAddress, true)

        }
        imgLeft?.clickWithDebounce {
            onBackPressed()
        }


        cardAddInfo?.clickWithDebounce {

            when (isFromNewCustAddress) {
                true -> {
                    // new customer addinfo click
                    startActivity(
                        Intent(
                            this,
                            NewAddressDetailsActivity::class.java
                        ).putExtra(Constants.isFromNewCustAddress, isFromNewCustAddress)
                    )
                }
                false -> {
                    // new supplier addinfo click
                    startActivity(
                        Intent(
                            this,
                            NewAddressDetailsActivity::class.java
                        ).putExtra(Constants.isFromNewCustAddress, isFromNewCustAddress)
                    )
                }
            }

        }

        tvTcs?.clickWithDebounce {

            startActivity(
                Intent(
                    this,
                    TCSActivity::class.java
                ).putExtra(Constants.isFromTcsNogAdd, true)
            )
        }
    }

    private fun setSuppContactorAddressData() {
        if (prefs.contains(Constants.PREF_ADD_EDIT_SUPP_SHIPPING_OTHER_KEY)) {

            val multipleCont_Ship_OtherType =
                object : TypeToken<ArrayList<ShippingOrOtherAddressModel>>() {}.type
            list_Supp_Cont_Ship_Other = Gson().fromJson(
                prefs[Constants.PREF_ADD_EDIT_SUPP_SHIPPING_OTHER_KEY, ""],
                multipleCont_Ship_OtherType
            )

            addContOrOther_v1.visibility = View.VISIBLE
            ll_addContOrOther_row.visibility = View.VISIBLE
            addContOrOther_v2.visibility = View.VISIBLE
            setupCustSuppAdapter(null, list_Supp_Cont_Ship_Other)

        } else {
            // hide items of contact/shipping  if pref is empty
            addContOrOther_v1.visibility = View.GONE
            ll_addContOrOther_row.visibility = View.GONE
            addContOrOther_v2.visibility = View.GONE

        }
    }

    private fun setCustContactorAddressData() {
        if (prefs.contains(Constants.PREF_ADD_EDIT_CUST_SHIPPING_OTHER_KEY)) {

            val multipleCont_Ship_OtherType =
                object : TypeToken<ArrayList<ShippingOrOtherAddressModel>>() {}.type
            list_Cust_Cont_Ship_Other = Gson().fromJson(
                prefs[Constants.PREF_ADD_EDIT_CUST_SHIPPING_OTHER_KEY, ""],
                multipleCont_Ship_OtherType
            )

            binding.addContOrOtherV1.visibility = View.VISIBLE
            binding.llAddContOrOtherRow.visibility = View.VISIBLE
            binding.addContOrOtherV2.visibility = View.VISIBLE
            setupCustSuppAdapter(list_Cust_Cont_Ship_Other, null)
        } else {
            // hide items of contact/shipping  if pref is empty
            binding.addContOrOtherV1.visibility = View.GONE
            binding.llAddContOrOtherRow.visibility = View.GONE
            binding.addContOrOtherV2.visibility = View.GONE
        }
    }

    private fun setupCustSuppAdapter(
        listCustAddress: ArrayList<ShippingOrOtherAddressModel>?,
        listSuppAddress: ArrayList<ShippingOrOtherAddressModel>?
    ) {
        // recyclerview nature of goods setup
        recyclerViewNatureGoods.visibility = View.VISIBLE
        txtNatureofGoodsNoEntries.visibility = View.GONE
        recyclerViewNatureGoods.layoutManager =
            LinearLayoutManager(this@AddContactOrAddressActivity)
        when (isFromNewCustAddress) {
            true -> {
                //adapter for new customer
                addEditContOrAddAdapter =
                    AddtionalInfoContOtherShipAdapter(list_Cust_Cont_Ship_Other, null)
            }
            false -> {
                //adapter for new supplier
                addEditContOrAddAdapter = AddtionalInfoContOtherShipAdapter(null, listSuppAddress)
            }
        }
        recyclerViewNatureGoods.adapter = addEditContOrAddAdapter
    }


    fun removeAddressItem(position: Int, isFromCustomer: Boolean) {
        if (isValidClickPressed()) {
            when (isFromCustomer) {
                true -> {
                    if (list_Cust_Cont_Ship_Other != null && list_Cust_Cont_Ship_Other!!.size > 0) {
                        if (position >= list_Cust_Cont_Ship_Other!!.size) {
                            //index not exists
                        } else {
                            // index exists
                            when (list_Cust_Cont_Ship_Other!!.get(position).type) {
                                "contact" -> {
                                    deleteFromPrefOrFromApi(
                                        list_Cust_Cont_Ship_Other!!.get(position).contact_person_info_id,
                                        position,
                                        isFromCustomer,
                                        "contact"
                                    )

                                }
                                "address" -> {
                                    deleteFromPrefOrFromApi(
                                        list_Cust_Cont_Ship_Other!!.get(position).contact_person_info_id,
                                        position,
                                        isFromCustomer,
                                        "address"
                                    )
                                }
                            }

                        }
                    }
                }
                false -> {
                    if (list_Supp_Cont_Ship_Other != null && list_Supp_Cont_Ship_Other!!.size > 0) {
                        if (position >= list_Supp_Cont_Ship_Other!!.size) {
                            //index not exists
                        } else {
                            // index exists
                            when (list_Supp_Cont_Ship_Other!!.get(position).type) {
                                "contact" -> {
                                    deleteFromPrefOrFromApi(
                                        list_Supp_Cont_Ship_Other!!.get(position).contact_person_info_id,
                                        position,
                                        isFromCustomer,
                                        "contact"
                                    )

                                }
                                "address" -> {
                                    deleteFromPrefOrFromApi(
                                        list_Cust_Cont_Ship_Other!!.get(position).contact_person_info_id,
                                        position,
                                        isFromCustomer,
                                        "address"
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private fun deleteFromPrefOrFromApi(
        id: String?,
        position: Int,
        isFromCustomer: Boolean,
        type: String?
    ) {
        when (isFromCustomer) {
            // customer
            true -> {
                when (id.isNullOrBlank()) {
                    true -> {
                        list_Cust_Cont_Ship_Other!!.removeAt(position)
                        addEditContOrAddAdapter.notifyDataSetChanged()

                        if (list_Cust_Cont_Ship_Other!!.size > 0) {
                            prefs[Constants.PREF_ADD_EDIT_CUST_SHIPPING_OTHER_KEY] =
                                Gson().toJson(list_Cust_Cont_Ship_Other)
                            // invoiceCalculation()
                        } else {
                            prefs.edit().remove(Constants.PREF_ADD_EDIT_CUST_SHIPPING_OTHER_KEY)
                                .apply()
                            //  linear_calculation_view_purchase.visibility = View.GONE
                        }

                    }
                    false -> {
                        deleteCustAddressItemApi(
                            position,
                            loginModel.data?.bearer_access_token,
                            type,
                            id
                        )
                    }
                }
            }
            // supplier
            false -> {
                when (id.isNullOrBlank()) {
                    true -> {
                        list_Supp_Cont_Ship_Other!!.removeAt(position)
                        addEditContOrAddAdapter.notifyDataSetChanged()



                        if (list_Supp_Cont_Ship_Other!!.size > 0) {
                            prefs[Constants.PREF_ADD_EDIT_SUPP_SHIPPING_OTHER_KEY] =
                                Gson().toJson(list_Supp_Cont_Ship_Other)
                            // invoiceCalculation()
                        } else {
                            prefs.edit().remove(Constants.PREF_ADD_EDIT_SUPP_SHIPPING_OTHER_KEY)
                                .apply()
                            //  linear_calculation_view_purchase.visibility = View.GONE
                        }

                    }
                    false -> {
                        deleteSuppAddressItemApi(
                            position,
                            loginModel.data?.bearer_access_token,
                            type,
                            id
                        )
                    }
                }
            }
        }
    }

    private fun deleteCustAddressItemApi(
        index: Int,
        token: String?,
        type: String?,
        edit_id: String?
    ) {
        if (NetworkUtils.isConnected()) {
            viewModel.deletecontactaddressinfo(token, type, edit_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                list_Cust_Cont_Ship_Other!!.removeAt(index)
                                addEditContOrAddAdapter.notifyDataSetChanged()
                                Toast.makeText(
                                    this,
                                    it.data?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                if (list_Cust_Cont_Ship_Other!!.size > 0) {
                                    prefs[Constants.PREF_ADD_EDIT_CUST_SHIPPING_OTHER_KEY] =
                                        Gson().toJson(list_Cust_Cont_Ship_Other)
                                    // invoiceCalculation()
                                } else {
                                    prefs.edit()
                                        .remove(Constants.PREF_ADD_EDIT_CUST_SHIPPING_OTHER_KEY)
                                        .apply()
                                    //  linear_calculation_view_purchase.visibility = View.GONE
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

    }

    private fun deleteSuppAddressItemApi(
        index: Int,
        token: String?,
        type: String?,
        edit_id: String?
    ) {
        if (NetworkUtils.isConnected()) {
            viewModel.deletecontactaddressinfo(token, type, edit_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                list_Supp_Cont_Ship_Other!!.removeAt(index)
                                addEditContOrAddAdapter.notifyDataSetChanged()
                                Toast.makeText(
                                    this,
                                    it.data?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                if (list_Supp_Cont_Ship_Other!!.size > 0) {
                                    prefs[Constants.PREF_ADD_EDIT_SUPP_SHIPPING_OTHER_KEY] =
                                        Gson().toJson(list_Cust_Cont_Ship_Other)

                                } else {
                                    prefs.edit()
                                        .remove(Constants.PREF_ADD_EDIT_SUPP_SHIPPING_OTHER_KEY)
                                        .apply()

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

    }

    private fun getLoginModelFromPrefs() {
        prefs = PreferenceHelper.defaultPrefs(applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter
    }

}